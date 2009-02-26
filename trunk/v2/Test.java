package v2;

import ao.Node;
import ao.Position;
import model.Board;
import v2.data.MovePicker;
import v2.state.Move;
import v2.state.Outcome;
import v2.state.State;
import v2.state.Status;

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
        System.out.println("nodes " +
                buildTree(new State(), 3));

        System.out.println("mobs  " + mobs);
        System.out.println("caps  " + caps);
        System.out.println("mates " + mates);
        System.out.println("draws " + draws);
        System.out.println("checks " + checks);

        //testRandom();
    }

    public static void testRandom()
    {
        // warmup
        for (int i = 0; i < 15001; i++)
        {
            playOutRandom( new State() );
//            playOutMediocre();
        }

        int  count  = 50000;
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


    //--------------------------------------------------------------------
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


    //--------------------------------------------------------------------
    private static int mobs   = 0;
    private static int caps   = 0;
    private static int checks = 0;
    private static int draws  = 0;
    private static int mates  = 0;

    private static int buildTree(
            State state, int ply)
    {
        if (ply == 0) return 1;

        int moves[] = new int[256];
        int nMoves  = state.moves(moves);
        if (nMoves == -1) return 0;

        int sum = 0;
        for (int i = 0; i < nMoves; i++) {
            State proto = state.prototype();
            int move    = Move.apply(moves[ i ], proto);
            int subMove = buildTree(proto, ply - 1);
            sum += subMove;

            if (subMove != 0) {
                boolean inCheck = proto.isInCheck( proto.nextToAct() );
                if (inCheck) checks++;

                if (Move.isCapture(move)) {
                    caps++;
                }
//                else if (Move.moveType(move) == MoveType.MOBILITY) {
//                    mobs++;
//                }
            }
        }
        if (sum == 0) {
            if (state.isInCheck( state.nextToAct() )) {
                mates++;
            } else {
                draws++;
            }
            return 1;
        }

        return sum;
    }


    //--------------------------------------------------------------------
    private static Outcome playOutRandom(State state)
    {
        Status status;
        int    nextCount = 0;
        int[]  nextMoves = new int[ 256 ];
        int[]  moves     = new int[ 256 ];
        int    nMoves    = state.moves(moves);

        while ((status = state.knownStatus()) == Status.IN_PROGRESS)
        {
            int     move     = 0;
            boolean madeMove = false;

            int[] moveOrder = MovePicker.pick(nMoves);
            for (int moveIndex : moveOrder)
            {
                move = Move.apply(moves[ moveIndex ], state);

                // generate opponent moves
                nextCount = state.moves(nextMoves);
                if (nextCount < 0) { // it lead to mate
                    System.out.println("Unmaking" + Move.toString(move));
                    Move.unApply(move, state);
                } else {
                    madeMove = true;
                    break;
                }
            }
            if (! madeMove) {
                return state.isInCheck(state.nextToAct())
                       ? Outcome.loses(state.nextToAct())
                       : Outcome.DRAW;
            }

            System.out.println(Move.toString(move));
            System.out.println(state);

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
