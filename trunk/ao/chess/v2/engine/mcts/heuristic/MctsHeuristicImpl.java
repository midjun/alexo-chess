package ao.chess.v2.engine.mcts.heuristic;

import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:54:01 PM
 */
public class MctsHeuristicImpl
    implements MctsHeuristic
{
    //--------------------------------------------------------------------
    @Override
    public double firstPlayEargency() {
        return 1000 + Math.random();
    }


    //--------------------------------------------------------------------
    @Override
    public int[] orderMoves(
            State fromState, int[] moves, int nMoves) {
        return MovePicker.pickRandom(nMoves);
    }
}
