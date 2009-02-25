package v2.move;

import v2.data.BitBoard;
import v2.data.BitLoc;

/**
 * Date: Feb 6, 2009
 * Time: 2:33:17 AM
 *
 * See http://chessprogramming.wikispaces.com/Pawn+Attacks
 */
public class Pawns
{
    //--------------------------------------------------------------------
    private Pawns() {}


    //--------------------------------------------------------------------
    private static final long[] WHITE;
    private static final long[] BLACK;

    static
    {
        WHITE = new long[64];
        BLACK = new long[64];

        for (int loc = 0; loc < 64; loc++) {
            WHITE[ loc ] = whiteAttacks(
                                BitLoc.locationToBitBoard(loc));

            BLACK[ loc ] = blackAttacks(
                                BitLoc.locationToBitBoard(loc));
        }
    }

    public static long whiteAttacks(int loc) {
        return WHITE[ loc ];
    }
    public static long blackAttacks(int loc) {
        return BLACK[ loc ];
    }


    //--------------------------------------------------------------------
    public static long whiteAttacks(long wPawn) {
        return BitBoard.noEaOne(wPawn) |
               BitBoard.noWeOne(wPawn);
    }

    public static long blackAttacks(long bPawn) {
        return BitBoard.soEaOne(bPawn) |
               BitBoard.soWeOne(bPawn);
    }


    //--------------------------------------------------------------------
    public static final BoardPiece WHITE_MOVES = new BoardPiece() {
        public long moves(long whitePawn,
                          long occupied,
                          long notOccupied,
                          long proponent,
                          long notProponent,
                          long opponent) {
            long mobility =
                    BitBoard.northOne(whitePawn) & notOccupied;
            if (mobility != 0 &&
                (whitePawn & BitBoard.RANK_2) != 0) {
                mobility |=
                        BitBoard.northOne(mobility) & notOccupied;
            }

            long captures = BitBoard.noEaOne(whitePawn) |
                            BitBoard.noWeOne(whitePawn);
            return mobility |
                   captures & opponent;
        }
    };


    //--------------------------------------------------------------------
    public static final BoardPiece BLACK_MOVES = new BoardPiece() {
        public long moves(long blackPawn,
                          long occupied,
                          long notOccupied,
                          long proponent,
                          long notProponent,
                          long opponent) {
            long mobility = BitBoard.soutOne(blackPawn);
            if ((blackPawn & BitBoard.RANK_7) != 0) {
                mobility |= BitBoard.soutOne(mobility);
            }

            long captures = BitBoard.soEaOne(blackPawn) |
                            BitBoard.soWeOne(blackPawn);
            return mobility & notOccupied |
                   captures & opponent;
        }
    };
}
