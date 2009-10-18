package ao.chess.v2.engine.mcts.rollout;

import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:56:58 PM
 */
public class MctsRolloutImpl
    implements MctsRollout
{
    //--------------------------------------------------------------------
//    public static class Factory implements MctsRollout.Factory {
//        @Override public MctsRollout newRollout() {
//            return new MctsRolloutImpl();
//        }
//    }


    //--------------------------------------------------------------------
    private final int nSims;


    //--------------------------------------------------------------------
    public MctsRolloutImpl()
    {
        this(1);
    }

    public MctsRolloutImpl(int accuracy)
    {
        nSims = accuracy;
    }


    //--------------------------------------------------------------------
    @Override public double monteCarloPlayout(
            State fromState, MctsHeuristic heuristic)
    {
//        return Math.random();
        double sum = 0;
        for (int i = 0; i < nSims; i++) {
            State curState =
                    (i != (nSims - 1))
                    ? fromState
                    : fromState.prototype();

            sum += computeMonteCarloPlayout(curState, heuristic);
        }
        return sum / nSims;
    }

    private double computeMonteCarloPlayout(
            State fromState, MctsHeuristic heuristic) {
        State   simState  = fromState;
        int     nextCount = 0;
        int[]   nextMoves = new int[ Move.MAX_PER_PLY ];
        int[]   moves     = new int[ Move.MAX_PER_PLY ];
        int     nMoves    = simState.moves(moves);
        Outcome outcome   = null;

        boolean wasDrawnBy50MovesRule = false;
        do
        {
//            if (! Representation.unpackStream(
//                    Representation.packStream(
//                            simState)).equals( simState )) {
//                System.out.println("PACKING ERROR!!!");
//                System.out.println(simState);
//            }

            int     move;
            boolean madeMove = false;

//            int[] moveOrder = heuristic.orderMoves(
//                    simState, moves, nMoves);
//            for (int moveIndex : moveOrder)
            for (int moveIndex = 0; moveIndex < nMoves; moveIndex++)
            {
                move = Move.apply(moves[ moveIndex ], simState);

                // generate opponent moves
                nextCount = simState.moves(nextMoves);

                if (nextCount < 0) { // if leads to mate
                    Move.unApply(move, simState);
                } else {
                    madeMove = true;
                    break;
                }
            }
            if (! madeMove) {
                outcome = simState.isInCheck(simState.nextToAct())
                          ? Outcome.loses(simState.nextToAct())
                          : Outcome.DRAW;
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        while (! (wasDrawnBy50MovesRule =
                    simState.isDrawnBy50MovesRule()));
        if (wasDrawnBy50MovesRule) {
            outcome = Outcome.DRAW;
        }

        return outcome == null
               ? Double.NaN
               : outcome.valueFor( simState.nextToAct() );
    }
}
