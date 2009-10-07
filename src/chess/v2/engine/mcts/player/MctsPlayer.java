package ao.chess.v2.engine.mcts.player;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.*;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.Move;
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
    private final MctsValue.Factory     values;
    private final MctsRollout           rollouts;
    private final MctsSelector          sellectors;
    private final MctsHeuristic         heuristics;
    private final MctsScheduler.Factory schedulers;

    private State    prevState = null;
    private MctsNode prevPlay  = null;


    //--------------------------------------------------------------------
    public <V extends MctsValue<V>>
            MctsPlayer(MctsNode.Factory<V>   nodeFactory,
                       MctsValue.Factory<V>  valueFactory,
                       MctsRollout           rolloutInstance,
                       MctsSelector<V>       selectorInstance,
                       MctsHeuristic         heuristicInstance,
                       MctsScheduler.Factory schedulerFactory)
    {
        nodes      = nodeFactory;
        values     = valueFactory;
        rollouts   = rolloutInstance;
        sellectors = selectorInstance;
        heuristics = heuristicInstance;
        schedulers = schedulerFactory;
    }


    //--------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        MctsNode root = null;
        if (prevState != null && prevPlay != null) {
            root = prevPlay.childMatching(
                    action(prevState, position));
        }

        if (root == null) {
            root = nodes.newNode(values);
        } else {
            Io.display("Recycling " + root);
        }

        MctsScheduler scheduler = schedulers.newScheduler(
                timeLeft, timePerMove, timeIncrement);

        int count  = 0;
        while (scheduler.shouldContinue()) {
            root.runTrajectory(position, values, rollouts, heuristics);

            if (count++ != 0 && count % 1 == 0) {
                Io.display( root.bestMove(sellectors).information() );
            }
        }

        MctsAction act = root.bestMove(sellectors);
        
        prevPlay = act.node();
        prevState = position.prototype();
        Move.apply(act.action(), prevState);

        return act.action();
    }


    //--------------------------------------------------------------------
    private int action(State from, State to)
    {
        int[] moves  = new int[ Move.MAX_PER_PLY ];
        int   nMoves = from.moves( moves );

        for (int i = 0; i < nMoves; i++) {
            int move = Move.apply(moves[i], from);
            if (from.equals( to )) {
                Move.unApply(move, from);
                return move;
            }
            Move.unApply(move, from);
        }
        return -1;
    }
}
