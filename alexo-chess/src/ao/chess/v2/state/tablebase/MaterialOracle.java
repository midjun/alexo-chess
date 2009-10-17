package ao.chess.v2.state.tablebase;

import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.time.Stopwatch;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.Serializable;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 13-Oct-2009
 * Time: 11:15:38 PM
 */
public class MaterialOracle implements Serializable
{
    //--------------------------------------------------------------------
    private final LongOpenHashSet blackWins = new LongOpenHashSet();
    private final LongOpenHashSet whiteWins = new LongOpenHashSet();


    //--------------------------------------------------------------------
    public MaterialOracle(
            final Oracle      oracle,
            final List<Piece> material)
    {
        System.out.println("Computing MaterialOracle for " + material);
        Stopwatch timer = new Stopwatch();

        StateMap states = new StateMap();
        new PositionTraverser().traverse(
                material, states);

        System.out.println("filled state map, took " + timer);
        timer = new Stopwatch();

        HashRetrograde retro = new HashRetrograde();
        states.traverse(retro);

        System.out.println("retrograte analysis done, took " + timer);
        timer = new Stopwatch();

        // all initial mates
        LongSet prevWhiteWins = new LongOpenHashSet();
        LongSet prevBlackWins = new LongOpenHashSet();

        addImmediateMates(
                states.states(), oracle, states,
                prevWhiteWins, prevBlackWins);

        int ply = 1;
        System.out.println("initial mates found, took " + timer);
        timer = new Stopwatch();

        while (! prevWhiteWins.isEmpty() ||
               ! prevBlackWins.isEmpty())
        {
            whiteWins.addAll( prevWhiteWins );
            blackWins.addAll( prevBlackWins );

            LongSet nextWhiteWins = new LongOpenHashSet();
            LongSet nextBlackWins = new LongOpenHashSet();

            addNextImmediates(states, retro, prevWhiteWins, oracle,
                              nextWhiteWins, nextBlackWins);
            addNextImmediates(states, retro, prevBlackWins, oracle,
                              nextWhiteWins, nextBlackWins);

            System.out.println(
                    "finished ply " + (ply++) + ", took " + timer);
            timer = new Stopwatch();

            prevWhiteWins = nextWhiteWins;
            prevBlackWins = nextBlackWins;
        }

        System.out.println("done, got " +
                blackWins.size() + " | " +
                whiteWins.size() + " | " +
                states.size());
    }


    //--------------------------------------------------------------------
    private void addNextImmediates(
            StateMap       allStates,
            HashRetrograde retro,
            LongSet        prevWins,
            Oracle         oracle,
            LongSet        nextWhiteWins,
            LongSet        nextBlackWins)
    {
        for (long wWin : prevWins) {
            addImmediateMates(
                    allStates.states(retro.precedents( wWin )),
                    oracle, allStates, nextWhiteWins, nextBlackWins);
        }
    }


    //--------------------------------------------------------------------
    private void addImmediateMates(
            Iterable<State> states,
            Oracle          oracle,
            StateMap        allStates,
            LongSet         nextWhiteWins,
            LongSet         nextBlackWins)
    {
        for (State state : states) {
            if (! isUnknown(state)) continue;

            Outcome result = findImminentMate(
                    state, allStates, oracle);
            if (result == Outcome.WHITE_WINS) {
                nextWhiteWins.add( state.staticHashCode() );
            } else if (result == Outcome.BLACK_WINS) {
                nextBlackWins.add( state.staticHashCode() );
            }
        }
    }

    private boolean isUnknown(State state) {
        long staticHash = state.staticHashCode();
        return ! (whiteWins.contains(staticHash) ||
                  blackWins.contains(staticHash));
    }


    //--------------------------------------------------------------------
    private Outcome findImminentMate(
            State state, StateMap allStates, Oracle oracle)
    {
        Outcome result = state.knownOutcome();
        if (result != null && result != Outcome.DRAW) {
            return result;
        }

        int legalMoves[] = state.legalMoves();
        if (legalMoves == null) return null;

        int ties = 0, losses = 0;
        for (int legalMove : legalMoves)
        {
            Move.apply(legalMove, state);

            long    staticHash     = state.staticHashCode();
            Outcome imminentResult =
                    allStates.containsState( staticHash )
                    ? see( staticHash )
                    : oracle.see(state);
            Move.unApply(legalMove, state);

            if (imminentResult != null &&
                    imminentResult != Outcome.DRAW) {
                if (state.nextToAct() == imminentResult.winner()) {
                    return imminentResult;
                } else {
                    losses++;
                }
            } else {
                ties++;
            }
        }

        return   ties   > 0 ? null
               : losses > 0 ? Outcome.loses( state.nextToAct() )
                            : null;
    }


    //--------------------------------------------------------------------
    public Outcome see(long staticHash) {
        return whiteWins.contains(staticHash)
               ? Outcome.WHITE_WINS
               : blackWins.contains(staticHash)
                 ? Outcome.BLACK_WINS
                 : null;
    }
}
