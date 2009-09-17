package v2.test;

import v2.engine.Player;
import v2.engine.uct.UctPlayer;
import v2.state.Move;
import v2.state.State;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 9:49:17 PM
 */
public class BrainTeaser {
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        int    time   = 1000;

        Player player = new UctPlayer(1024);

        State  state  = new State();

        int move = player.move(state, time, time, 0);
        Move.apply(move, state);
        player.move(state, time, time, 0);

        System.out.println(state);
        System.out.println(Move.toString(
                player.move(state, time, time, 0)
        ));
    }
}
