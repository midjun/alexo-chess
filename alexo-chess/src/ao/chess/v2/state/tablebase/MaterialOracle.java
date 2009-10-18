package ao.chess.v2.state.tablebase;

import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.io.Serializable;

/**
 * User: alex
 * Date: 18-Oct-2009
 * Time: 5:59:21 PM
 */
public interface MaterialOracle extends Serializable
{
    public Outcome see(long staticHash);

    public Outcome see(State state);
}