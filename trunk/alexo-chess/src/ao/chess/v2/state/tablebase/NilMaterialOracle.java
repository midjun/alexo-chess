package ao.chess.v2.state.tablebase;

import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 18-Oct-2009
 * Time: 5:59:38 PM
 */
public class NilMaterialOracle implements MaterialOracle
{
    @Override public Outcome see(long staticHash) {
        return null;
    }

    @Override public Outcome see(State state) {
        return null;
    }
}
