package ao.chess.v2.engine.mcts.rollout;

import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.state.tablebase.Oracle;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:56:58 PM
 */
public class MctsTablebaseRollout
    implements MctsRollout
{
    //--------------------------------------------------------------------
//    public static class Factory implements MctsRollout.Factory {
//        @Override public MctsRollout newRollout() {
//            return new MctsRolloutImpl();
//        }
//    }

    private long invocationCount = 0;
    private long   tableHitCount = 0;


    //--------------------------------------------------------------------
//    private final int    nPieces;
    private final Oracle oracle;


    //--------------------------------------------------------------------
    public MctsTablebaseRollout()
    {
        this(3);
    }

    public MctsTablebaseRollout(int endgamePieces)
    {
//        nPieces = endgamePieces;
        oracle  = new Oracle(endgamePieces);
    }


    //--------------------------------------------------------------------
    @Override public double monteCarloPlayout(
            State fromState, MctsHeuristic heuristic)
    {
        if (++invocationCount % 1000 == 0) {
            System.out.println("table hit fraction: " +
                    ((double) tableHitCount) / invocationCount);
        }
        
        Colour  pov       = fromState.nextToAct();
        State   simState  = fromState;
        int     nextCount = 0;
        int[]   nextMoves = new int[ Move.MAX_PER_PLY ];
        int[]   moves     = new int[ Move.MAX_PER_PLY ];
        int     nMoves    = simState.moves(moves);
        Outcome outcome;

        boolean tableHitUpdated = false;
        boolean wasDrawnBy50MovesRule = false;
        do
        {
            int     move;
            boolean madeMove = false;

            if ((! tableHitUpdated) &&
                    simState.pieceCount() <= 3) {
                tableHitCount++;
                tableHitUpdated = true;
            }

            outcome = oracle.see(simState);
            if (outcome != null) {
//                tableHitCount++;
                break;
            }

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
               : outcome.valueFor( pov );
    }
}