package ao.chess.v2.state;

/**
 * User: alexo
 * Date: Feb 21, 2009
 * Time: 10:18:24 PM
 */
public enum CastleType
{
    //--------------------------------------------------------------------
    QUEEN_SIDE, KING_SIDE;


    //--------------------------------------------------------------------
    public static final CastleType[] VALUES = values();


    //--------------------------------------------------------------------
    public static class Set
    {
        //----------------------------------------------------------------
        private final boolean whiteQueen;
        private final boolean whiteKing;
        private final boolean blackQueen;
        private final boolean blackKing;


        //----------------------------------------------------------------
        public Set(byte fromBits)
        {
            this(((fromBits >>> 3) & 1) == 1,
                 ((fromBits >>> 2) & 1) == 1,
                 ((fromBits >>> 1) & 1) == 1,
                 ( fromBits        & 1) == 1
                );
        }

        public Set(boolean whiteQueenSide,
                   boolean whiteKingSide,
                   boolean blackQueenSide,
                   boolean blackKingSide)
        {
            whiteQueen = whiteQueenSide;
            whiteKing  = whiteKingSide;
            blackQueen = blackQueenSide;
            blackKing  = blackKingSide;
        }

        
        //----------------------------------------------------------------
        public byte toBits() {
            return (byte)(
                   (whiteQueen ? 1 : 0) << 3 |
                   (whiteKing  ? 1 : 0) << 2 |
                   (blackQueen ? 1 : 0) << 1 |
                   (blackKing  ? 1 : 0));
        }


        //----------------------------------------------------------------
        public String toFen() {
            StringBuilder str = new StringBuilder();

            if (whiteKing) {
                str.append("K");
            }
            if (whiteQueen) {
                str.append("Q");
            }
            if (blackKing) {
                str.append("k");
            }
            if (blackQueen) {
                str.append("q");
            }

            return str.toString();
        }
    }
}
