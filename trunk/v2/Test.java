package v2;

import ao.Node;
import ao.Position;
import model.Board;
import v2.state.Move;
import v2.state.Outcome;
import v2.state.State;
import v2.state.Status;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Feb 6, 2009
 * Time: 5:57:42 PM
 */
public class Test
{
    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        // warmup
        for (int i = 0; i < 15001; i++)
        {
            playOutRandom( new State() );
        }

        int  count  = 100000;
        long before = System.currentTimeMillis();
        for (int i = 0; i < count; i++)
        {
            //System.out.println(i);
            playOutRandom( new State() );
//            playOutMediocre();
        }
        long delta = System.currentTimeMillis() - before;
        System.out.println(
                count + " at " +
               (count / (delta / 1000)) + " per second");
    }

    private static final Map<Position, Node>
            transposition = new HashMap<Position, Node>();
    private static void playOutMediocre()
    {
        Board board = new Board();
        board.setupStart();
        Node  root  = new Node(board);

        root.strategize(
                board,
                transposition,
                false);
    }

    private static Outcome playOutRandom(State state)
    {
        Status status;
        int    nextCount;
        int[]  nextMoves = new int[ 256 ];
        int[]  moves     = new int[ 256 ];
        int    nMoves    = state.moves(moves);
        BitSet seen      = new BitSet(nMoves);

        while ((status = state.knownStatus()) == Status.IN_PROGRESS)
        {
            int     move;
            boolean allFailed = false;
            do
            {
                // apply new random move
                int random;
                do {
                    random = (int)(Math.random() * nMoves);
                } while (seen.get(random));
                seen.set(random);

                move = moves[ random ];
                move = Move.apply(move, state);

                // generate opponent moves
                nextCount = state.moves(nextMoves);
                if (nextCount < 0) { // it lead to mate
//                    System.out.println("Unmaking" + Move.toString(move));
                    Move.unApply(move, state);
                    allFailed = (seen.cardinality() == nMoves);
                } else break;
            }
            while (! allFailed);
            if (allFailed) {
                return state.isInCheck(state.nextToAct())
                       ? Outcome.loses(state.nextToAct())
                       : Outcome.DRAW;
            } else {
                seen.clear();
            }

//            System.out.println(Move.toString(move));
//            System.out.println(state);

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        return status.toOutcome();
    }
}
