package ao.chess.v2.engine.mcts.player;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.MctsNode;
import ao.chess.v2.engine.mcts.MctsScheduler;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 12:52:02 PM
 */
public class MctsPlayer implements Player
{
    //--------------------------------------------------------------------
    private final MctsNode.Factory      nodes;
    private final MctsScheduler.Factory schedulers;


    //--------------------------------------------------------------------
    public MctsPlayer(MctsNode.Factory      nodeFactory,
                      MctsScheduler.Factory schedulerFactory)
    {
        nodes      = nodeFactory;
        schedulers = schedulerFactory;
    }


    //--------------------------------------------------------------------
    @Override public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        MctsNode      root      = nodes.newNode();
        MctsScheduler scheduler = schedulers.newScheduler(
                timeLeft, timePerMove, timeIncrement);

        int count  = 0;
        while (scheduler.shouldContinue()) {
            root.runTrajectory(position);

            if (count++ != 0 && count % 25000 == 0) {
                root.displayBestMoveStatus();
            }
        }

        MctsAction act = root.bestMove();
        return act.action();
    }
}
