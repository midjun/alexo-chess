package ao.chess.v2.state.tablebase;

import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 10:01:26 PM
 */
public class Oracle
{
    public Outcome see(State position) {
        position.atMostPieces(3);
        return null;
    }
}
