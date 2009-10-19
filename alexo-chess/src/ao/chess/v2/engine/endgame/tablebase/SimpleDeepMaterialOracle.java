package ao.chess.v2.engine.endgame.tablebase;

import ao.chess.v2.engine.endgame.bitbase.BitOracle;
import ao.chess.v2.engine.endgame.common.MaterialRetrograde;
import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.engine.endgame.common.StateMap;
import ao.chess.v2.engine.endgame.common.index.MinPerfectHash;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.time.Stopwatch;
import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;

/**
 * User: aostrovsky
 * Date: 13-Oct-2009
 * Time: 11:15:38 PM
 */
public class SimpleDeepMaterialOracle implements DeepMaterialOracle
{
    //--------------------------------------------------------------------
    private final MinPerfectHash     indexer;
    private final byte[]             outcomes;


    //--------------------------------------------------------------------
    public SimpleDeepMaterialOracle(
            final DeepOracle oracle,
            final List<Piece> material)
    {
        System.out.println("Computing MaterialOracle for " + material);
        Stopwatch timer = new Stopwatch();

        int materialTally = MaterialTally.tally(material);
        indexer           = new MinPerfectHash(material);

        System.out.println("computed perfect hash, took " + timer);
        timer = new Stopwatch();

        StateMap states = new StateMap(indexer);
        new PositionTraverser().traverse(
                material, states);

        System.out.println("filled state map " +
                states.size() + ", took " + timer);
        timer = new Stopwatch();

        MaterialRetrograde retro =
                new MaterialRetrograde(materialTally, indexer);
        states.traverse(retro);

        System.out.println("retrograde analysis done, took " + timer);
        timer = new Stopwatch();

        // initial mates
        outcomes = new byte[ states.size() ];

        IntSet prevWhiteWins = new IntOpenHashSet();
        IntSet prevBlackWins = new IntOpenHashSet();

        addImmediateMates(
                states.states(), oracle, materialTally,
                prevWhiteWins, prevBlackWins);

        int ply = 1;
        System.out.println("initial mates found, took " + timer);
        timer = new Stopwatch();

        while (! prevWhiteWins.isEmpty() ||
               ! prevBlackWins.isEmpty())
        {
            ply++;
            addAll( true , prevWhiteWins, ply );
            addAll( false, prevBlackWins, ply );

            IntSet nextWhiteWins = new IntOpenHashSet();
            IntSet nextBlackWins = new IntOpenHashSet();

            addNextImmediates(states, materialTally, retro,
                    prevWhiteWins, oracle, nextWhiteWins, nextBlackWins);
            addNextImmediates(states, materialTally, retro,
                    prevBlackWins, oracle, nextWhiteWins, nextBlackWins);

            System.out.println(
                    "finished ply " + (ply++) + ", took " + timer);
            timer = new Stopwatch();

            prevWhiteWins = nextWhiteWins;
            prevBlackWins = nextBlackWins;
        }

        System.out.println("done, got " +
                blackWinCount() + " | " +
                whiteWinCount() + " | " +
                states.size());
    }

    private void addAll(boolean whiteWins, IntSet indexes, int ply) {
        for (int index : indexes) {
            outcomes[index] = (byte)
                    ((whiteWins)
                     ? Math.min( ply, Byte.MAX_VALUE)
                     : Math.max(-ply, Byte.MIN_VALUE));
        }
    }


    //--------------------------------------------------------------------
    private void addNextImmediates(
            StateMap           allStates,
            int                materialTally,
            MaterialRetrograde retro,
            IntSet             prevWins,
            DeepOracle         oracle,
            IntSet             nextWhiteWins,
            IntSet             nextBlackWins)
    {
        for (int prevWin : prevWins) {
            addImmediateMates(
                    allStates.states(retro.indexPrecedents( prevWin )),
                    oracle, materialTally, nextWhiteWins, nextBlackWins);
        }
    }


    //--------------------------------------------------------------------
    private void addImmediateMates(
            Iterable<State> states,
            DeepOracle      oracle,
            int             materialTally,
            IntSet          nextWhiteWins,
            IntSet          nextBlackWins)
    {
        for (State state : states) {
            if (! isWinKnown(state)) continue;

            DeepOutcome result = findImminentMate(
                    state, materialTally, oracle);
            if (result.whiteWins()) {
                nextWhiteWins.add(
                        indexer.index(state.staticHashCode()) );
            } else if (result.blackWins()) {
                nextBlackWins.add(
                        indexer.index(state.staticHashCode()) );
            }
        }
    }

    private boolean isWinKnown(State state) {
        long staticHash = state.staticHashCode();
        int  index      = indexer.index( staticHash );
        return outcomes[ index ] != 0;
    }


    //--------------------------------------------------------------------
    private DeepOutcome findImminentMate(
            State state, int materialTally, DeepOracle oracle)
    {
        Outcome result = state.knownOutcome();
        if (result != null && result != Outcome.DRAW) {
            return new DeepOutcome(result, 1);
        }

        int legalMoves[] = state.legalMoves();
        if (legalMoves == null) return null;

        int ties = 0;
        int minLossPly = Integer.MAX_VALUE;
        int minWinPly  = Integer.MAX_VALUE;
        for (int legalMove : legalMoves)
        {
            Move.apply(legalMove, state);

            long        staticHash     = state.staticHashCode();
            DeepOutcome imminentResult =
                    materialTally == state.tallyAllMaterial()
                    ? see( staticHash )
                    : oracle.see(state);
            Move.unApply(legalMove, state);

            if (imminentResult != null &&
                    ! imminentResult.isDraw()) {
                if (state.nextToAct() ==
                        imminentResult.outcome().winner()) {
                    minWinPly = Math.min(minWinPly,
                            imminentResult.plyDistance() + 1);
//                    return imminentResult;
                } else {
//                    losses++;
                    minLossPly = Math.min(minLossPly,
                            imminentResult.plyDistance() + 1);
                }
            } else {
                ties++;
            }
        }

        if (minWinPly != Integer.MAX_VALUE) {
            return new DeepOutcome(
                    Outcome.wins(state.nextToAct()),
                    minWinPly);
        } else if (ties > 0) {
            return null;
        } else /*if (minLossPly != Integer.MAX_VALUE)*/ {
            return new DeepOutcome(
                    Outcome.loses( state.nextToAct() ),
                    minLossPly);
        }
    }


    //--------------------------------------------------------------------
    private int whiteWinCount() {
        int count = 0;
        for (byte outcome : outcomes) {
            if (outcome > 0) {
                count++;
            }
        }
        return count;
    }

    private int blackWinCount() {
        int count = 0;
        for (byte outcome : outcomes) {
            if (outcome < 0) {
                count++;
            }
        }
        return count;
    }


    //--------------------------------------------------------------------
    public DeepOutcome see(long staticHash) {
        int index   = indexer.index( staticHash );
        int outcome = outcomes[index];
        return outcome > 0
               ? new DeepOutcome(Outcome.WHITE_WINS, outcome)
               : outcome < 0
                 ? new DeepOutcome(Outcome.BLACK_WINS, -outcome)
                 : null;
    }

    @Override public DeepOutcome see(State state) {
        return see( state.staticHashCode() );
    }
}