package v2.test;

import ao.AlexoChess;
import ao.FullOutcome;
import ao.Node;
import model.Board;
import v2.data.MovePicker;
import v2.state.Move;
import v2.state.Outcome;
import v2.state.State;
import v2.state.Status;

/**
 * Date: Feb 6, 2009
 * Time: 5:57:42 PM
 */
public class Test
{
    //--------------------------------------------------------------------
    private static GameBranch fenA = new GameBranch();
    private static GameBranch fenB = new GameBranch();

    public static void main(String[] args)
    {
//        int nodesA = buildTree(new State(), 4, fenA);
////        int nodesB = buildMediocreTree(3, fenB);
//
//        System.out.println("nodesA "  + nodesA);
////        System.out.println("nodesB "  + nodesB);
//
////        System.out.println("mobs  "  + mobs);
//        System.out.println("caps  "  + caps);
//        System.out.println("mates "  + mates);
//        System.out.println("draws "  + draws);
//        System.out.println("checks " + checks);
//
////        System.out.println(fenA.equals( fenB ));
////        System.out.println(fenB.delta(  fenA ));
////        System.out.println(fenA);


        testRandom();
    }


    //--------------------------------------------------------------------
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
//    private static int mobs   = 0;
    private static int caps   = 0;
    private static int checks = 0;
    private static int draws  = 0;
    private static int mates  = 0;

    private static int buildTree(
            State state, int ply, GameBranch check)
    {
        int moves[] = new int[256];
        int nMoves  = state.legalMoves(moves);
        if (nMoves == 0) {
            if (state.isInCheck(state.nextToAct())) {
                mates++;
            } else {
                draws++;
            }
            return 1;
        }
        if (ply == 0) return 1;

        int sum = 0;
        for (int i = 0; i < nMoves; i++) {
            State proto = state.prototype();
            int   move  = Move.apply(moves[ i ], proto);

            if (ply == 1) {
                if (proto.isInCheck(proto.nextToAct())) {
                    checks++;
                }
                if (Move.isCapture(move)) {
                    caps++;
                }
            }

            sum += buildTree(
                    proto, ply - 1,
                    null
//                    check.add(
//                            truncate(proto.toFen()),
//                            Move.toString(move))
                    );
        }

        return sum;
    }

//    private static int buildTree(
//            State state, int ply)
//    {
//        if (ply == 0) return 1;
//
//        int moves[] = new int[256];
//        int nMoves  = state.moves(moves);
//        if (nMoves == -1) return 0;
//
//        int sum = 0;
//        for (int i = 0; i < nMoves; i++) {
//            State proto = state.prototype();
//            int move    = Move.apply(moves[ i ], proto);
//            int subMove = buildTree(proto, ply - 1);
//            sum += subMove;
//
//            if (subMove != 0) {
//                boolean inCheck = proto.isInCheck( proto.nextToAct() );
//                if (inCheck) checks++;
//
//                if (Move.isCapture(move)) {
//                    caps++;
//                }
////                else if (Move.moveType(move) == MoveType.MOBILITY) {
////                    mobs++;
////                }
//            }
//        }
//        if (sum == 0) {
//            if (state.isInCheck( state.nextToAct() )) {
//                mates++;
//            } else {
//                draws++;
//            }
//            return 1;
//        }
//
//        return sum;
//    }


    //--------------------------------------------------------------------
    private static int buildMediocreTree(
            int ply, GameBranch check)
    {
        Board board = new Board();
        board.setupStart();
        return buildMediocreTree(board, ply, check);
    }
    private static int buildMediocreTree(
            Board board, int ply, GameBranch check)
    {
        if (ply == 0) return 1;

        FullOutcome out = AlexoChess.outcome(board, null, 0);
        if (out.isDraw()) {
            draws++;
            return 1;
        } else if (out != FullOutcome.UNDECIDED) {
            mates++;
        }

        int moves[] = new int[256];
        int nMoves  = board.generateMoves(false, moves, 0);

        int sum = 0;
        for (int i = 0; i < nMoves; i++)
        {
            int move = moves[ i ];
            board.makeMove(move);

            sum += buildMediocreTree(board, ply - 1,
                    check.add(truncate(board.getFEN()),
                              model.Move.notation( move )));

            board.unmakeMove(move);
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
//                    System.out.println("Unmaking " + Move.toString(move));
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


    //--------------------------------------------------------------------
//    private static boolean fenEqual(String fenA, String fenB)
//    {
//        String sansCountA = fenA.replaceAll(" \\w+$", "");
//        String sansCountB = fenB.replaceAll(" \\w+$", "");
//
//        return sansCountA.equals(
//                sansCountB);
//    }
    private static String truncate(String fen)
    {
        return fen.replaceAll(" .*? \\d+ \\w+$", "");
    }


    //--------------------------------------------------------------------
//    private static final Map<Position, Node>
//            transposition = new HashMap<Position, Node>();
    private static void playOutMediocre()
    {
        Board board = new Board();
        board.setupStart();
        Node  root  = new Node(board);

        root.strategize(
                board,
                null,
                false);
    }
}
