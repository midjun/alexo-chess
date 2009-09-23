package ao.chess.v2.engine.uct;

import ao.chess.v1.util.Io;
import ao.chess.v2.data.MovePicker;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.state.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 8:58:45 PM
 */
public class UctNode
{
    //--------------------------------------------------------------------
    private final boolean optimize;

    private int      visits;
    private double   rewardSum;

    private State    state;
    private UctNode  kids[];
    private int      acts[];




    //--------------------------------------------------------------------
    public UctNode(boolean             opt,
                   State               protoState,
                   Map<State, UctNode> transposition)
    {
        optimize = opt;

        if (protoState == null) {
            state = null;
        } else {
            state = protoState.prototype();
        }

        visits    = 0;
        rewardSum = 0;

        if (optimize) {
            addTo(transposition);
        }
    }


    //--------------------------------------------------------------------
    public int size()
    {
        if (kids == null) return 1;

        int size = 0;
        for (UctNode nextChild : kids)
        {
            size += nextChild.size();
        }
        return size + 1;
    }

    public int depth()
    {
        if (kids == null) return 0;

        int depth = 0;
        for (UctNode kid : kids)
        {
            depth = Math.max(depth, kid.depth());
        }
        return depth + 1;
    }

    public int visits()
    {
        return visits;
    }


    //--------------------------------------------------------------------
    public void addTo(Map<State, UctNode> transposition)
    {
        if (! optimize) return;
//        transposition.put(state, this);
    }

    public void addLineageTo(Map<State, UctNode> transposition)
    {
        addTo( transposition );

        if (kids == null) return;
        for (UctNode kid : kids)
        {
            kid.addLineageTo( transposition );
        }
    }


    //--------------------------------------------------------------------
    public UctNode childMatching(State board)
    {
        if (kids == null) return null;

        for (UctNode kid : kids)
        {
            if (board.equals( kid.state ))
            {
                return kid;
            }
        }
        return null;
    }



    //--------------------------------------------------------------------
    public Action optimize()
    {
        if (kids == null) return null;

        UctNode optimal       = this;
        int     optimalAct    = 0;
        double  optimalReward = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < kids.length; i++)
        {
            UctNode nextChild = kids[ i ];
            int     act       = acts[ i ];
            double  reward    = nextChild.averageReward();

            if (reward > optimalReward)
            {
                optimal       = nextChild;
                optimalAct    = act;
                optimalReward = reward;
            }
        }

//        Io.display("best move is " + optimal.averageReward() +
//                    " with " + optimal.visits() +
//                    " at " + (optimize ? "?" : optimal.depth()));
        Io.display("best move is " + optimal.averageReward() +
                    " with " + optimal.visits() +
                    " at " + optimal.depth());
        return new Action(optimalAct, optimal);
    }


    //--------------------------------------------------------------------
    private double averageReward()
    {
        return visits == 0
               ? 0
               : rewardSum / visits;
    }

    private boolean unvisited()
    {
        return visits == 0;
    }


    //--------------------------------------------------------------------
    public void strategize(Map<State, UctNode> transposition)
    {
        List<Action> path = new ArrayList<Action>();
        path.add(new Action(0, this));

        while (! path.get( path.size() - 1 ).node().unvisited())
        {
            UctNode node = path.get( path.size() - 1 ).node();
            Action selectedChild =
                    node.descendByUCB1( transposition );
            if (selectedChild == null) break;

            path.add( selectedChild );
        }

        Action  leaf     = path.get( path.size() - 1 );
        UctNode leafNode = leaf.node();
        if (leafNode.state == null) {
            Action parent = path.get( path.size() - 2 );
            State parentState = parent.node().state.prototype();
            Move.apply(leaf.act(), parentState);
            leafNode.state = parentState;
        }
        propagateValue(
                path, leafNode.monteCarloValue());
    }

    private void propagateValue(
            List<Action> path, double reward)
    {
        double maxiMax = 1.0 - reward;
//        double maxiMax = reward;
        for (int i = path.size() - 1; i >= 0; i--)
        {
            Action step = path.get(i);

            step.NODE.rewardSum += maxiMax;
            step.NODE.visits++;

            maxiMax = 1.0 - maxiMax;
        }
    }


    //--------------------------------------------------------------------
    private Action descendByUCB1(Map<State, UctNode> transposition)
    {
        if (kids == null)
        {
            populateKids(transposition);
        }

        double greatestUtc   = Long.MIN_VALUE;
        int    greatestChild = -1;
        for (int i = 0; i < kids.length; i++)
        {
            UctNode kid = kids[ i ];
//            int     act = acts[ i ];

            double utcValue;
            if (kid.unvisited())
            {
                utcValue = 1000 + Math.random();
            }
            else
            {
//                if (optimize) {
//                    utcValue =
//                        kid.averageReward() +
//                        Math.sqrt((2 * Math.log(visits)) /
//                                  kid.visits) +
//                        2;
//                } else {
                    utcValue =
                        kid.averageReward() +
                        Math.sqrt(Math.log(visits) /
                                  (5 * kid.visits));
//                }
            }

            if (utcValue > greatestUtc)
            {
                greatestUtc   = utcValue;
                greatestChild = i;
            }
        }

        return greatestChild == -1
               ? null
               : new Action(
                    acts[greatestChild],
                    kids[greatestChild]);
    }


    //--------------------------------------------------------------------
    private double monteCarloValue()
    {
        State   simState  = state.prototype();
        Status  status    = null;
        int     nextCount = 0;
        int[]   nextMoves = new int[ 128 ];
        int[]   moves     = new int[ 128 ];
        int     nMoves    = state.moves(moves);
        Outcome outcome   = null;

        do
        {
            int     move;
            boolean madeMove = false;

            int[] moveOrder = MovePicker.pickRandom(nMoves);
            for (int moveIndex : moveOrder)
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
        while ((status = simState.knownStatus()) == Status.IN_PROGRESS);

        if (outcome == null && status != null) {
            outcome = status.toOutcome();
        }

        return outcome == null
               ? Double.NaN
               : outcome.valueFor(state.nextToAct());
    }


    //--------------------------------------------------------------------
    private void populateKids(Map<State, UctNode> transposition)
    {
        if (kids != null) return;

        int[] legalMoves = new int[Move.MAX_PER_PLY];
        int   moveCount  = state.legalMoves(legalMoves);

        kids = new UctNode[ moveCount ];
        acts = new int    [ moveCount ];

        for (int i = 0; i < moveCount; i++)
        {
//            if (optimize) {
                kids[ i ] = new UctNode(optimize, null, transposition);
                acts[ i ] = legalMoves[ i ];
//            } else {
//                int move = Move.apply(legalMoves[ i ], state);
//
//                UctNode existing = transposition.get(state);
//                if (existing == null) {
//                    kids[ i ] = new UctNode(optimize, state, transposition);
//                } else {
//                    kids[ i ] = existing;
//                }
//                acts[ i ] = move;
//
//                Move.unApply(move, state);
//            }
        }
    }


    //--------------------------------------------------------------------
    public static class Action
    {
        private final int     ACT;
        private final UctNode NODE;

        public Action(int act, UctNode node)
        {
            ACT  = act;
            NODE = node;
        }

        public int act()
        {
            return ACT;
        }
        public UctNode node()
        {
            return NODE;
        }
    }
}
