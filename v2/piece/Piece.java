package v2.piece;

import v2.move.*;

/**
 * Date: Feb 6, 2009
 * Time: 5:32:19 PM
 */
public enum Piece implements BoardPiece
{
    //--------------------------------------------------------------------
    WHITE_PAWN  (Colour.WHITE, Figure.PAWN,   Pawns.WHITE_MOVES),
    WHITE_KNIGHT(Colour.WHITE, Figure.KNIGHT, Knights.MOVES),
    WHITE_BISHOP(Colour.WHITE, Figure.BISHOP, Bishops.MOVES),
    WHITE_ROOK  (Colour.WHITE, Figure.ROOK,   Rooks.MOVES),
    WHITE_QUEEN (Colour.WHITE, Figure.QUEEN,  Queens.MOVES),
    WHITE_KING  (Colour.WHITE, Figure.KING,   Kings.MOVES),

    BLACK_PAWN  (Colour.BLACK, Figure.PAWN,   Pawns.BLACK_MOVES),
    BLACK_KNIGHT(Colour.BLACK, Figure.KNIGHT, Knights.MOVES),
    BLACK_BISHOP(Colour.BLACK, Figure.BISHOP, Bishops.MOVES),
    BLACK_ROOK  (Colour.BLACK, Figure.ROOK,   Rooks.MOVES),
    BLACK_QUEEN (Colour.BLACK, Figure.QUEEN,  Queens.MOVES),
    BLACK_KING  (Colour.BLACK, Figure.KING,   Kings.MOVES);


    //--------------------------------------------------------------------
    public  static final Piece[]   VALUES           = values();
    private static final Piece[][] BY_COLOUR_FIGURE;
    static
    {
        BY_COLOUR_FIGURE = new Piece[ Colour.VALUES.length ]
                                    [ Figure.VALUES.length ];
        for (Colour c : Colour.VALUES)
        {
            for (Figure f : Figure.VALUES)
            {
                for (Piece p : VALUES)
                {
                    if (p.colour() == c && p.figure() == f)
                    {
                        BY_COLOUR_FIGURE[ c.ordinal() ]
                                        [ f.ordinal() ] = p;
                        break;
                    }
                }
            }
        }
    }

    public static Piece valueOf(Colour colour, Figure figure)
    {
        return BY_COLOUR_FIGURE[ colour.ordinal() ]
                               [ figure.ordinal() ];
    }


    //--------------------------------------------------------------------
    private final Colour     COLOUR;
    private final Figure     FIGURE;
    private final BoardPiece MOVES;

    private Piece(Colour     colour,
                  Figure     figure,
                  BoardPiece moves)
    {
        COLOUR = colour;
        FIGURE = figure;
        MOVES  = moves;
    }


    //--------------------------------------------------------------------
    public long moves(long pieceLocation,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent) {
        return MOVES.moves(
                pieceLocation, occupied, notOccupied,
                proponent, notProponent, opponent);
    }

    public Colour colour()
    {
        return COLOUR;
    }
    public boolean isWhite()
    {
        return COLOUR == Colour.WHITE;
    }

    public Figure figure()
    {
        return FIGURE;
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return COLOUR == Colour.WHITE
               ? FIGURE.toString().toUpperCase()
               : FIGURE.toString().toLowerCase();
    }
}
