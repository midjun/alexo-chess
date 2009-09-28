package ao.chess.v2.engine.mcts;

import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 12:59:18 PM
 */
public interface MctsNode//<T extends MctsNode<T>>
{
    //--------------------------------------------------------------------
    public void runTrajectory(
            State         fromProtoState,
            MctsHeuristic heuristic,
            MctsRollout   mcRollout);

    public void displayBestMoveStatus();

    public MctsAction bestMove();

//    public T descendByBandit();


    //--------------------------------------------------------------------
    public static interface Factory
            <//N extends MctsNode<N>,
             V extends MctsValue<V>>
    {
        public MctsNode/*N*/ newNode(MctsValue.Factory<V> valueFactory);
    }
}
