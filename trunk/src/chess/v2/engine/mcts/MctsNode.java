package ao.chess.v2.engine.mcts;

import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 12:59:18 PM
 */
public interface MctsNode<V extends MctsValue<V>>
{
    //--------------------------------------------------------------------
    public void runTrajectory(
            State                fromProtoState,
            MctsValue.Factory<V> values,
            MctsRollout          mcRollout,
            MctsHeuristic        heuristic);

//    public void displayBestMoveStatus(MctsSelector<V> selector);

    public MctsAction<V> bestMove(MctsSelector<V> selector);

    public MctsNode childMatching(int action);

//    public T descendByBandit();


    //--------------------------------------------------------------------
    public static interface Factory
            <//N extends MctsNode<N>,
             V extends MctsValue<V>>
    {
        public MctsNode<V>/*N*/ newNode(MctsValue.Factory<V> valueFactory);
    }
}
