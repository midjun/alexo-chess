package v2.move;

/**
 * Date: Feb 6, 2009
 * Time: 11:04:14 PM
 */
public interface BoardPiece
{
    /*
     * occupied
     * not occupied
     * not own pieces
     */

    // attacks and occupancies
    //  moves will be overlapping with own pieces
    public long moves(long pieceLocation,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent);
}
