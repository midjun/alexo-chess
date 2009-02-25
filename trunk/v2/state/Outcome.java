package v2.state;

import v2.piece.Colour;

/**
 * User: alexo
 * Date: Feb 22, 2009
 * Time: 12:34:57 PM
 */
public enum Outcome
{
    //--------------------------------------------------------------------
    DRAW,
    WHITE_WINS,
    BLACK_WINS;


    //--------------------------------------------------------------------
    public static Outcome wins(Colour c)
    {
        return c == Colour.WHITE
               ? WHITE_WINS : BLACK_WINS;
    }

    public static Outcome loses(Colour c)
    {
        return wins(c.invert());
    }
}
