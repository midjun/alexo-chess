package v2.move;

/**
 * Date: Feb 6, 2009
 * Time: 4:37:00 AM
 */
public class Queens implements BoardPiece
{
    //--------------------------------------------------------------------
    private Queens() {}

    public static final BoardPiece MOVES = new Queens();


    //--------------------------------------------------------------------
    public long moves(long queen,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return   Rooks.MOVES.moves(
                    queen, occupied, notOccupied,
                    proponent, notProponent, opponent) |
               Bishops.MOVES.moves(
                    queen, occupied, notOccupied,
                    proponent, notProponent, opponent);
    }
}
