package v2.engine.uct;

import ao.*;
import model.Board;
import old.Evaluation;
import util.Io;
import v2.state.Move;
import v2.state.State;
import v2.state.Status;
import v2.state.Outcome;
import v2.piece.Colour;
import v2.data.MovePicker;

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
    private int      visits;
    private double   rewardSum;

    private State    state;
    private UctNode  kids[];
    private int      acts[];


    //--------------------------------------------------------------------
    public UctNode(State protoState)
    {
        state     = protoState.prototype();
        visits    = 0;
        rewardSum = 0;
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
    public UctNode childMatching(State board)
    {
        if (kids == null) return null;

        for (UctNode kid : kids)
        {
            if (kid.state.equals( board ))
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
        double  optimalReward = Long.MIN_VALUE;
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
    public void strategize()
    {
        List<Action> path = new ArrayList<Action>();
        path.add(new Action(0, this));

        while (! path.get( path.size() - 1 ).NODE.unvisited())
        {
            UctNode node = path.get( path.size() - 1 ).NODE;
            Action selectedChild =
                    node.descendByUCB1();
            if (selectedChild == null) break;

            path.add( selectedChild );
        }

        UctNode leaf = path.get( path.size() - 1 ).NODE;
        propagateValue(
                path,
                leaf.monteCarloValue());
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
    private Action descendByUCB1()
    {
        if (kids == null)
        {
            populateKids();
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
//                utcValue =
//                        Integer.MAX_VALUE
//                            + ((Move.capture(act) != 0)
//                                ? 100000 : 1000)
//                              * Math.random();
                utcValue = 1.0;
            }
            else
            {
                utcValue =
                    kid.averageReward() +
                    Math.sqrt(Math.log(visits) /
                              (5 * kid.visits));
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
    private void populateKids()
    {
        if (kids != null) return;

        int[] legalMoves = new int[Move.MAX_PER_PLY];
        int   moveCount  = state.legalMoves(legalMoves);

        kids = new UctNode[ moveCount ];
        acts = new int    [ moveCount ];

        for (int i = 0; i < moveCount; i++)
        {
            int move = Move.apply(legalMoves[ i ], state);

            UctNode newChild = new UctNode(state);
            kids[ i ] = newChild;
            acts[ i ] = move;

            Move.unApply(move, state);
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


    //--------------------------------------------------------------------
//    public static void randomBench(Board state)
//    {
//        int[] legalMoves = new int[128];
//        do
//        {
//            int moveCount = state.generateMoves(false, legalMoves, 0);
//            if (moveCount > 0)
//            {
//                int move = RandomBot.random(legalMoves, moveCount);
//                state.makeMove(move);
//            }
//        }
//        while (AlexoChess.outcome(state, null, 0)
//                 == FullOutcome.UNDECIDED );
//    }
}
