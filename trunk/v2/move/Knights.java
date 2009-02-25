package v2.move;

import v2.data.BitBoard;
import v2.data.BitLoc;

/**
 * Date: Feb 6, 2009
 * Time: 3:17:06 AM
 */
public class Knights implements BoardPiece
{
    //----------------------------------------------------------
    private Knights() {}

    public static final BoardPiece MOVES = new Knights();


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


    //----------------------------------------------------------
    public static long attacks(long knight) {
        return BitBoard.offset(knight,  2,  1) |
               BitBoard.offset(knight,  2, -1) |
               BitBoard.offset(knight, -2,  1) |
               BitBoard.offset(knight, -2, -1);
    }


    //----------------------------------------------------------
    public long moves(long knight,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return attacks(knight) & notProponent;
    }
}
