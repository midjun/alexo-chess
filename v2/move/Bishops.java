package v2.move;

import static v2.move.SlidingPieces.slide;

/**
 * Date: Feb 6, 2009
 * Time: 4:34:58 AM
 */
public class Bishops implements BoardPiece
{
    //--------------------------------------------------------------------
    private Bishops() {}

    public static final Bishops MOVES = new Bishops();


    //--------------------------------------------------------------------
//    public static long attacks(long bishop,
//                               long proponentPieces,
//                               long opponentPieces) {
//        return slide(bishop,  1,  1, proponentPieces, opponentPieces) |
//               slide(bishop,  1, -1, proponentPieces, opponentPieces) |
//               slide(bishop, -1,  1, proponentPieces, opponentPieces) |
//               slide(bishop, -1, -1, proponentPieces, opponentPieces);
//    }


    //--------------------------------------------------------------------
    public long moves(long bishop,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return slide(bishop,  1,  1, notOccupied, opponent) |
               slide(bishop,  1, -1, notOccupied, opponent) |
               slide(bishop, -1,  1, notOccupied, opponent) |
               slide(bishop, -1, -1, notOccupied, opponent);
    }
}
