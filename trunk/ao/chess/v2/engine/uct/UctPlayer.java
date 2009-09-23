package ao.chess.v2.engine.uct;

import ao.chess.v2.engine.Player;
import ao.chess.v2.state.State;
import util.Io;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 7:31:39 PM
 */
public class UctPlayer implements Player
{
    //--------------------------------------------------------------------
    private final int     nSim;
    private       UctNode prevRoot;


    //--------------------------------------------------------------------
    public UctPlayer(int nSimulations)
    {
        nSim     = nSimulations;
        prevRoot = null;
    }


    //--------------------------------------------------------------------
    public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        UctNode root = null;
        if (prevRoot != null) {
            root = prevRoot.childMatching(position);
        }
        if (root == null) {
            root = new UctNode(position);
        }
        Io.display("Recycling " + root.visits() +
                    "@" + root.depth());
        for (int i = 0; i < nSim; i++) {
            root.strategize();
        }

        UctNode.Action act = root.optimize();
        prevRoot = act.node();
        return act.act();
    }
}
