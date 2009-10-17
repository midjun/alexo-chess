package ao.chess.v2.engine.mcts.player;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.*;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

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
    private final TranspositionTable    transTable;

    private State              prevState = null;
    private MctsNode           prevPlay  = null;


    //--------------------------------------------------------------------
    public <V extends MctsValue<V>>
            MctsPlayer(MctsNode.Factory<V>   nodeFactory,
                       MctsValue.Factory<V>  valueFactory,
                       MctsRollout           rollOutInstance,
                       MctsSelector<V>       selectorInstance,
                       MctsHeuristic         heuristicInstance,
                       TranspositionTable<V> transpositionTable,
                       MctsScheduler.Factory schedulerFactory)
    {
        nodes       = nodeFactory;
        values      = valueFactory;
        rollouts    = rollOutInstance;
        sellectors  = selectorInstance;
        heuristics  = heuristicInstance;
        transTable  = transpositionTable;
        schedulers  = schedulerFactory;
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
            root = nodes.newNode(position, values);
            transTable.retain( LongLists.EMPTY_LIST );
        } else {
            Io.display("Recycling " + root);

//            LongSet states = new LongOpenHashSet();
//            root.addStates(states);
//            Io.display("Unique states " + states.size());
//            transTable.retain(states);
        }

        MctsScheduler scheduler = schedulers.newScheduler(
                timeLeft, timePerMove, timeIncrement);

        int  count  = 0;
        long lastReport = System.currentTimeMillis();
        while (scheduler.shouldContinue()) {
            root.runTrajectory(
                    position, values, rollouts, transTable, heuristics);

//            if (count++ != 0 && count % 10000 == 0) {
//                long timer  = System.currentTimeMillis() - lastReport;
//                long before = System.currentTimeMillis();
//                Io.display( root );
//                Io.display( root.bestMove(sellectors).information() );
//                Io.display( "took " + timer + " | " +
//                        (System.currentTimeMillis() - before) );
//                lastReport = System.currentTimeMillis();
//            }
        }

        MctsAction act = root.bestMove(sellectors);
        if (act == null) return -1; // game is done

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
