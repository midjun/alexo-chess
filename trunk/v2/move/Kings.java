package v2.move;

import v2.data.BitBoard;
import v2.data.BitLoc;

/**
 * Date: Feb 6, 2009
 * Time: 3:30:53 AM
 */
public class Kings implements BoardPiece
{
    //--------------------------------------------------------------------
    private Kings() {}

    public static final BoardPiece MOVES = new Kings();


    //--------------------------------------------------------------------
    private static final long[] CACHE;

    static
    {
        CACHE = new long[64];

        for (int loc = 0; loc < 64; loc++) {
            CACHE[ loc ] = attacks(
                    BitLoc.locationToBitBoard(loc));
        }
    }

    public static long attacks(int loc) {
        return CACHE[ loc ];
    }


    //--------------------------------------------------------------------
    public static long attacks(long king) {
        return BitBoard.offset(king,  1,  1) |
               BitBoard.offset(king,  1,  0) |
               BitBoard.offset(king,  1, -1) |
               BitBoard.offset(king,  0,  1) |
               BitBoard.offset(king,  0, -1) |
               BitBoard.offset(king, -1,  1) |
               BitBoard.offset(king, -1,  0) |
               BitBoard.offset(king, -1, -1);
    }


    //--------------------------------------------------------------------
    public long moves(long king,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return attacks(king) & notProponent;
    }
}
