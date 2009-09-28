package ao.chess.v2.engine.mcts.node;

import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsNode;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.engine.mcts.MctsValue;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 5:19:10 PM
 */
public class MctsNodeImpl<V extends MctsValue<V>>
        implements MctsNode//<MctsNodeImpl<V>>
{
    //--------------------------------------------------------------------
    public static class Factory<V extends MctsValue<V>>
            implements MctsNode.Factory<V> {
        @Override
        public MctsNodeImpl<V> newNode(
                        MctsValue.Factory<V> valueFactory)
        {
            return new MctsNodeImpl<V>(valueFactory);
        }

    }


    //--------------------------------------------------------------------
    private V                 value;
    private int[]             acts;
    private MctsNodeImpl<V>[] kids;


    //--------------------------------------------------------------------
    public MctsNodeImpl(MctsValue.Factory<V> valueFactory) {
        value = valueFactory.newValue();
        acts  = null;
        kids  = null;
    }


    //--------------------------------------------------------------------
    public boolean isUnvisited() {
        return acts == null;
    }


    //--------------------------------------------------------------------
    @Override
    public void runTrajectory(
            State         fromProtoState,
            MctsHeuristic heuristic,
            MctsRollout   mcRollout)
    {
        State cursor = fromProtoState.prototype();

        List<MctsNodeImpl> path = new ArrayList<MctsNodeImpl>();
        path.add(this);

        while (! path.get( path.size() - 1 ).isUnvisited())
        {
            MctsNodeImpl node = path.get( path.size() - 1 );

            MctsNodeImpl selectedChild =
                    node.descendByBandit(cursor, heuristic);
            if (selectedChild == null) break;

            path.add( selectedChild );
        }

        MctsNodeImpl leaf = path.get( path.size() - 1 );
        backupMcValue(
                path, mcRollout.monteCarloPlayout(cursor, heuristic));
    }


    //--------------------------------------------------------------------
    private MctsNodeImpl descendByBandit(
            State         fromState,
            MctsHeuristic heuristic)
    {
        if (kids == null) {
            initiateKids(fromState);
        }
        if (kids.length == 0) return null;

        double greatestValue      = Double.NEGATIVE_INFINITY;
        int    greatestValueIndex = -1;
        for (int i = 0; i < kids.length; i++) {
            MctsNodeImpl<V> kid = kids[ i ];

            double banditValue;
            if (kid.isUnvisited()) {
                banditValue = heuristic.firstPlayEargency();
            } else {
                banditValue = kid.value.confidenceBound(value);
            }

            if (banditValue > greatestValue) {
                greatestValue      = banditValue;
                greatestValueIndex = i;
            }
        }
        return kids[ greatestValueIndex ];
    }

    @SuppressWarnings("unchecked")
    private void initiateKids(State fromState) {
        acts = fromState.legalMoves();
        kids = new MctsNodeImpl[ acts.length ];
    }


    //--------------------------------------------------------------------
    private void backupMcValue(
            List<MctsNodeImpl> path,
            double             leafPlayout)
    {
        double reward = 1.0 - leafPlayout;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            path.get(i).value.update(reward);
            reward = 1.0 - reward;
        }
    }


    //--------------------------------------------------------------------
//    private double monteCarloPlayout() {
//        return
//    }



    //--------------------------------------------------------------------
    @Override
    public void displayBestMoveStatus() {

    }


    //--------------------------------------------------------------------
    @Override
    public MctsAction bestMove() {
        return null;
    }
}
