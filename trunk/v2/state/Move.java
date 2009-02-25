package v2.state;

import v2.piece.Colour;
import v2.piece.Figure;
import v2.piece.FigurePair;
import v2.piece.Piece;

/**
 * Date: Feb 6, 2009
 * Time: 6:21:41 PM
 */
public class Move
{
    //--------------------------------------------------------------------
    private Move() {}


    //--------------------------------------------------------------------
    public static void main(String[] args) {
        int move = mobility(Piece.WHITE_QUEEN,
                            0, 1, (byte) 0xF, (byte) 10);

        System.out.println("colour: " + colour(move));
        System.out.println("type: " + moveType(move));
        System.out.println("from: " + fromSquareIndex(move));
        System.out.println("to: " + toSquareIndex(move));
        System.out.println("main: " + figures(move).main() );
        System.out.println("cap: " + figures(move).captured() );
        System.out.println("en pass: " + enPassantRank(move) );
        System.out.println("promo: " + promotion(move) );
        System.out.println("castle: " + castleType(move) );
        System.out.println("avail castle: " + availCastles(move) );
        System.out.println("reverse: " + reversibleMoves(move) );

//        System.out.println(Integer.toBinaryString(
//                colourMask( Colour.WHITE )));
//
//        System.out.println(Integer.toBinaryString(
//                typeMask( MoveType.MOBILITY )));
//
//        System.out.println(Integer.toBinaryString(
//                figureMask( Figure.QUEEN )));
//
//        System.out.println(Integer.toBinaryString(
//                fromMask( 1 )));
//
//        System.out.println(Integer.toBinaryString(
//                toMask( 2 )));
//
//        System.out.println(Integer.toBinaryString(
//                availCastleMask( (byte) 0xF )));
//
//        System.out.println(Integer.toBinaryString(
//                reversibleMask( (byte) 10 )));
    }


    //--------------------------------------------------------------------
    /*
     * Layout of move int is:
     *  [ side
     *     lg 2 = 1   |
     *
     *    type {mobility, capture, en passant, castle}
     *      lg 4  = 2 |
     *
     *    from index
     *      lg 64 = 6 |
     *
     *    to index
     *      lg 64 = 6 |
     *
     *    FigurePair (used for mobility AND captures)
     *      lg 30 = 5  |
     *
     *    en passant file (if en passant)
     *      lg 8 = 3  |
     *
     *    promotion to figure {knight, bishop, rook, queen}
     *      lg 4 = 2  |
     *
     *    castle type (if castle)
     *      lg 2 = 1  |
     * 
     *    castling rights (for undo) [used for RMC]
     *      lg 16 = 4
     *
     *    extra [used for RMC]
     *      2 bits
     *  ]
     *
     * totalling 31 bits.
     */

    // making this final raises annoying suggestions
    private static       int COLOUR_SHIFT = 0;
    private static final int COLOUR_SIZE  = 1;
    private static final int COLOUR_MASK  =
            mask(COLOUR_SIZE) << COLOUR_SHIFT;

    private static final int TYPE_SHIFT = COLOUR_SHIFT + COLOUR_SIZE;
    private static final int TYPE_SIZE  = 2;
    private static final int TYPE_MASK  = mask(TYPE_SIZE) << TYPE_SHIFT;

    private static final int FROM_SHIFT = TYPE_SHIFT + TYPE_SIZE;
    private static final int FROM_SIZE  = 6;
    private static final int FROM_MASK  = mask(FROM_SIZE) << FROM_SHIFT;

    private static final int TO_SHIFT = FROM_SHIFT + FROM_SIZE;
    private static final int TO_SIZE  = 6;
    private static final int TO_MASK  = mask(TO_SIZE) << TO_SHIFT;

    private static final int FIGURE_SHIFT = TO_SHIFT + TO_SIZE;
    private static final int FIGURE_SIZE  = 5;
    private static final int FIGURE_MASK  =
            mask(FIGURE_SIZE) << FIGURE_SHIFT;
    private static final int FIGURE_MASK_NOT = ~FIGURE_MASK;

    private static final int EN_PASS_SHIFT = FIGURE_SHIFT + FIGURE_SIZE;
    private static final int EN_PASS_SIZE  = 3;
    private static final int EN_PASS_MASK  =
            mask(EN_PASS_SIZE) << EN_PASS_SHIFT;

    private static final int PROMO_SHIFT = EN_PASS_SHIFT + EN_PASS_SIZE;
    private static final int PROMO_SIZE  = 2;
    private static final int PROMO_MASK  =
            mask(PROMO_SIZE) << PROMO_SHIFT;

    private static final int CASTLE_SHIFT = PROMO_SHIFT + PROMO_SIZE;
    private static final int CASTLE_SIZE  = 1;
    private static final int CASTLE_MASK  =
            mask(CASTLE_SIZE) << CASTLE_SHIFT;

    private static final int AVAIL_CASTLE_SHIFT =
            CASTLE_SHIFT + CASTLE_SIZE;
    private static final int AVAIL_CASTLE_SIZE  = 4;
    private static final int AVAIL_CASTLE_MASK  =
            mask(AVAIL_CASTLE_SIZE) << AVAIL_CASTLE_SHIFT;

    private static final int REVERSE_SHIFT = CASTLE_SHIFT;
    private static final int REVERSE_SIZE  = 7;
    private static final int REVERSE_MASK  =
            mask(REVERSE_SIZE) << REVERSE_SHIFT;


    //--------------------------------------------------------------------
    private static int mask(int size) {
        int mask = 0;
        for (int i = 0; i < size; i++) mask |= 1 << i;
        return mask;
    }


    //--------------------------------------------------------------------
    private static int colourMask(Colour colour) {
        return colour.ordinal();
    }
    private static int typeMask(MoveType moveType) {
        return moveType.ordinal() << TYPE_SHIFT;
    }
    private static int fromMask(int squareIndex) {
        return squareIndex << FROM_SHIFT;
    }
    private static int toMask(int squareIndex) {
        return squareIndex << TO_SHIFT;
    }
    private static int figureMask(Figure figure) {
        
        return figureMask(FigurePair.valueOf(figure));
    }
    private static int figureMask(FigurePair figures) {
        return figures.ordinal() << FIGURE_SHIFT;
    }
    private static int enPassantMask(int enPassantFile) {
        return enPassantFile << EN_PASS_SHIFT;
    }
    private static int promotionMask(Figure promotingTo) {
        return (promotingTo.ordinal() - 1) << PROMO_SHIFT;
    }
    private static int castleMask(CastleType castle) {
        return castle.ordinal() << CASTLE_SHIFT;
    }
    private static int availCastleMask(byte availeableCastles) {
        return availeableCastles << AVAIL_CASTLE_SHIFT;
    }
    private static int reversibleMask(byte reversibleMoveCount) {
        return reversibleMoveCount << REVERSE_SHIFT;
    }


    //--------------------------------------------------------------------
    private static Colour colour(int move) {
        int index = (move & COLOUR_MASK) >>> COLOUR_SHIFT;
        return Colour.VALUES[ index ];
    }
    private static MoveType moveType(int move) {
        int index = (move & TYPE_MASK) >>> TYPE_SHIFT;
        return MoveType.VALUES[ index ];
    }
    private static int fromSquareIndex(int move) {
        return (move & FROM_MASK) >>> FROM_SHIFT;
    }
    private static int toSquareIndex(int move) {
        return (move & TO_MASK) >>> TO_SHIFT;
    }
    private static FigurePair figures(int move) {
        int index = (move & FIGURE_MASK) >>> FIGURE_SHIFT;
        return FigurePair.VALUES[ index ];
    }
    private static int enPassantRank(int move) {
        return (move & EN_PASS_MASK) >>> EN_PASS_SHIFT;
    }
    private static Figure promotion(int move) {
        int index = (move & PROMO_MASK) >>> PROMO_SHIFT;
        return Figure.VALUES[ index + 1 ];
    }
    private static CastleType castleType(int move) {
        int index = (move & CASTLE_MASK) >>> CASTLE_SHIFT;
        return CastleType.VALUES[ index ];
    }
    private static int availCastles(int move) {
        return (move & AVAIL_CASTLE_MASK) >>> AVAIL_CASTLE_SHIFT;
    }
    private static byte reversibleMoves(int move) {
        return (byte) ((move & REVERSE_MASK) >>> REVERSE_SHIFT);
    }


    //--------------------------------------------------------------------
    private static int addCaptured(
            int toMove, Figure captured) {

        FigurePair fp = figures(toMove);
        return (toMove & FIGURE_MASK_NOT) |
                figureMask(fp.withCaptured(captured));
    }


    //--------------------------------------------------------------------
    public static int mobility(
            Piece moving,
            int   fromSquareIndex,
            int   toSquareIndex,
            byte  availCastles,
            byte  reversibleMoves)
    {
        return   typeMask( MoveType.MOBILITY ) |
               colourMask( moving.colour()   ) |
               figureMask( moving.figure()   ) |
                 fromMask( fromSquareIndex   ) |
                   toMask( toSquareIndex     ) |
          availCastleMask( availCastles      ) |
           reversibleMask( reversibleMoves   );
    }

    public static int capture(
            Piece moving,
            int   fromSquareIndex,
            int   toSquareIndex,
            byte  availCastles,
            byte  reversibleMoves)
    {
        return   typeMask( MoveType.CAPTURE  ) |
               colourMask( moving.colour()   ) |
               figureMask( moving.figure()   ) |
                 fromMask( fromSquareIndex   ) |
                   toMask( toSquareIndex     ) |
          availCastleMask( availCastles      ) |
           reversibleMask( reversibleMoves   );
    }

    public static int enPassant()
    {
        return 0;
    }

    public static int castle()
    {
        return 0;
    }

    public static int promotion()
    {
        return 0;
    }


    //--------------------------------------------------------------------
    public static int apply(int move, State toState)
    {
        Colour colour = colour(move);
        switch (moveType(move))
        {
            case MOBILITY: {
                Figure figure = figures(move).main();
                int    from   = fromSquareIndex(move);
                int    to     = toSquareIndex(move);

                toState.mobalize(Piece.valueOf(colour, figure),
                                 from, to);
                return move;
            }

            case CAPTURE: {
                Figure attacker = figures(move).main();
                int    from     = fromSquareIndex(move);
                int    to       = toSquareIndex(move);

                Figure captured = toState.capture(
                        Piece.valueOf(colour, attacker), from, to);
                return addCaptured(move, captured);
            }
        }
        System.out.println("unable to handle move");
        return move;
    }


    //--------------------------------------------------------------------
    public static void unApply(int move, State toState)
    {
        Colour colour = colour(move);
        switch (moveType(move))
        {
            case MOBILITY: {
                Figure figure = figures(move).main();
                int    from   = fromSquareIndex(move);
                int    to     = toSquareIndex(move);
                int    castle = availCastles(move);
                byte   rev    = reversibleMoves(move);

                toState.unMobalize(
                        Piece.valueOf(colour, figure),
                        from, to, castle, rev);
                return;
            }

            case CAPTURE: {
                FigurePair figures  = figures(move);
                int        from     = fromSquareIndex(move);
                int        to       = toSquareIndex(move);
                int        castle = availCastles(move);
                byte       rev    = reversibleMoves(move);

                toState.unCapture(
                        Piece.valueOf(colour, figures.main()),
                        Piece.valueOf(colour.invert(), figures.captured()),
                        from, to, castle, rev);
                return;
            }
        }
        System.out.println("unable to handle move");
    }


    //--------------------------------------------------------------------
    public static String toString(int move)
    {
        switch (moveType(move))
        {
            case MOBILITY: {
                Figure figure = figures(move).main();
                int    from   = fromSquareIndex(move);
                int    to     = toSquareIndex(move);

                return "Making mobility move with " + figure +
                         " from " + from + " to " + to;
            }

            case CAPTURE: {
                Figure attacker = figures(move).main();
                int    from     = fromSquareIndex(move);
                int    to       = toSquareIndex(move);

                return "Making capture move with " + attacker +
                        " from " + from + " to " + to;
            }
        }

        return "Unknown";
    }
}
