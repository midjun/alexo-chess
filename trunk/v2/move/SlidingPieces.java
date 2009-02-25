package v2.move;

import static v2.data.BitBoard.offset;

/**
 * Date: Feb 6, 2009
 * Time: 3:39:16 AM
 */
public class SlidingPieces
{
    //--------------------------------------------------------------------
    private SlidingPieces() {}


    //--------------------------------------------------------------------
    public static long slide(long piece,
                             int  deltaRanks,
                             int  deltaFiles,
                             long notOccupied,
                             long opponentPieces)
    {
        long trail = 0;
        long cursor;
        for (cursor = offset(piece, deltaRanks, deltaFiles);
            (cursor & notOccupied) != 0;
             cursor = offset(cursor, deltaRanks, deltaFiles)) {
            trail |= cursor;
        }

        if ((cursor & opponentPieces) != 0) {
            trail |= cursor;
        }

        return trail;
    }
}
