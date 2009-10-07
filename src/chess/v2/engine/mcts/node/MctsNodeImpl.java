package ao.chess.v2.engine.mcts.node;

import ao.chess.v2.engine.mcts.*;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 5:19:10 PM
 */
public class MctsNodeImpl<V extends MctsValue<V>>
        implements MctsNode<V>
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
            State                fromProtoState,
            MctsValue.Factory<V> values,
            MctsRollout          mcRollout,
            MctsHeuristic        heuristic)
    {
        State cursor = fromProtoState.prototype();

        List<MctsNodeImpl> path = new ArrayList<MctsNodeImpl>();
        path.add(this);

        while (! path.get( path.size() - 1 ).isUnvisited())
        {
            MctsNodeImpl node = path.get( path.size() - 1 );

            MctsNodeImpl selectedChild =
                    node.descendByBandit(cursor, heuristic, values);
            if (selectedChild == null) break;

            path.add( selectedChild );
        }

        MctsNodeImpl leaf = path.get( path.size() - 1 );
        if (leaf.kids == null) {
            leaf.initiateKids(cursor);
        }

        backupMcValue(
                path, mcRollout.monteCarloPlayout(cursor, heuristic));
    }


    //--------------------------------------------------------------------
    private MctsNodeImpl descendByBandit(
            State                cursor,
            MctsHeuristic        heuristic,
            MctsValue.Factory<V> values)
    {
        if (kids.length == 0) return null;

        double greatestValue      = Double.NEGATIVE_INFINITY;
        int    greatestValueIndex = -1;
        for (int i = 0; i < kids.length; i++) {
            MctsNodeImpl<V> kid = kids[ i ];

            double banditValue;
            if (kid == null || kid.isUnvisited()) {
                banditValue = heuristic.firstPlayEargency();
            } else {
                banditValue = kid.value.confidenceBound(value);
            }

            if (banditValue > greatestValue) {
                greatestValue      = banditValue;
                greatestValueIndex = i;
            }
        }
        if (greatestValueIndex == -1) return null;

        if (kids[ greatestValueIndex ] == null) {
            kids[ greatestValueIndex ] = new MctsNodeImpl<V>(values);
        }
        Move.apply(acts[greatestValueIndex], cursor);
        return kids[ greatestValueIndex ];
    }

    @SuppressWarnings("unchecked")
    private void initiateKids(State fromState) {
        acts = fromState.legalMoves();
        kids = (acts == null)
               ? null : new MctsNodeImpl[ acts.length ];
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
    @Override
    public MctsAction<V> bestMove(MctsSelector<V> selector) {
        if (kids == null || kids.length == 0) return null;

        int             bestAct = -1;
        MctsNodeImpl<V> bestKid = null;
        for (int i = 0, kidsLength = kids.length; i < kidsLength; i++) {
            MctsNodeImpl<V> kid = kids[i];
            if (kid != null && (bestKid == null ||
                    selector.compare(
                            bestKid.value, kid.value) < 0)) {
                bestKid = kid;
                bestAct = acts[i];
            }
        }
        return new MctsAction<V>(bestAct, bestKid);
    }


    //--------------------------------------------------------------------
    @Override
    public MctsNode childMatching(int action) {
        if (acts == null) return null;

        for (int i = 0, actsLength = acts.length; i < actsLength; i++) {
            int act = acts[i];
            if (act == action) {
                return kids[i];
            }
        }
        
        return null;
    }


    //--------------------------------------------------------------------
    @Override
    public String toString() {
        return depth() + " | " + value.toString();
    }

    private int depth() {
        if (kids == null) return 0;

        int depth = 0;
        for (MctsNodeImpl kid : kids) {
            if (kid == null) continue;
            depth = Math.max(depth, kid.depth());
        }
        return depth + 1;
    }
}
