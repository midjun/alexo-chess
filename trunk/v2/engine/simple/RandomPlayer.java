package v2.engine.simple;

import v2.engine.PlayerImpl;
import v2.state.State;

/**
 * User: aostrovsky
 * Date: 14-Sep-2009
 * Time: 4:13:18 PM
 */
public class RandomPlayer extends PlayerImpl
{
    //--------------------------------------------------------------------
    private final int[] moves = new int[128];


    //--------------------------------------------------------------------
    public int move(
            State position)
    {
        int nMoves = position.legalMoves(moves);
        return moves[(int)(Math.random() * nMoves)];
    }
}
