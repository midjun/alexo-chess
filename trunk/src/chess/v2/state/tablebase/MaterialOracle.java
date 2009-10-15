package ao.chess.v2.state.tablebase;

import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.misc.Traverser;
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
    private final LongSet blackWins = new LongOpenHashSet();
    private final LongSet whiteWins = new LongOpenHashSet();


    //--------------------------------------------------------------------
    public MaterialOracle(
            final Oracle      oracle,
            final List<Piece> material) {

        Retrograde retro = new Retrograde();
        new PositionTraverser().traverse(
                material, retro);

        // all initial mates
        final LongSet nextBlackWins = new LongOpenHashSet();
        final LongSet nextWhiteWins = new LongOpenHashSet();
        new PositionTraverser().traverse(
                material, new Traverser<State>() {
                    @Override public void traverse(State state) {
                        Outcome result = findImminentMate(
                                state, material, oracle);
                        if (result == Outcome.WHITE_WINS) {
                            nextWhiteWins.add( state.staticHashCode() );
                        } else if (result == Outcome.BLACK_WINS) {
                            nextBlackWins.add( state.staticHashCode() );
                        }
                    }
                });

        while (! nextWhiteWins.isEmpty() &&
               ! nextBlackWins.isEmpty())
        {
            whiteWins.addAll( nextWhiteWins );
            blackWins.addAll( nextBlackWins );

            for (long wWin : nextWhiteWins) {
                // todo: left off here
                boolean whiteToAct = State.hashOfWhiteToAct(wWin);
//                retro.precedents()
            }

            nextWhiteWins.clear();
            nextBlackWins.clear();
        }



    }


    //--------------------------------------------------------------------
    private Outcome findImminentMate(
            State state, List<Piece> material, Oracle oracle)
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
            Outcome imminentResult =
                        material.containsAll( state.material() )
                        ? see(state.staticHashCode())
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


//    //--------------------------------------------------------------------
//    public static class OptimalPlay
//    {
//        private final Outcome OUT;
//        private final int     PLY;
//
//
//        public OptimalPlay(Outcome out, int ply) {
//            OUT = out;
//            PLY = ply;
//        }
//
//
//        public Outcome outcome() {
//            return OUT;
//        }
//
//        public int ply() {
//            return PLY;
//        }
//    }
}
