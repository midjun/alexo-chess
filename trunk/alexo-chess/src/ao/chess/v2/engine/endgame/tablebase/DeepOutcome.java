package ao.chess.v2.engine.endgame.tablebase;

import ao.chess.v2.state.Outcome;

/**
 * User: aostrovsky
 * Date: 19-Oct-2009
 * Time: 4:50:19 PM
 */
public class DeepOutcome
{
    //--------------------------------------------------------------------
    public static final DeepOutcome DRAW =
            new DeepOutcome(Outcome.DRAW, -1);


    //--------------------------------------------------------------------
    private final Outcome OUTCOME;
    private final int     PLY_AWAY;


    //--------------------------------------------------------------------
    public DeepOutcome(Outcome outcome, int plyAway)
    {
        OUTCOME  = outcome;
        PLY_AWAY = plyAway;
    }


    //--------------------------------------------------------------------
    public Outcome outcome() {
        return OUTCOME;
    }

    public int plyDistance() {
        return PLY_AWAY;
    }


    //--------------------------------------------------------------------
    public boolean isDraw() {
        return OUTCOME == Outcome.DRAW;
    }

    public boolean whiteWins() {
        return OUTCOME == Outcome.WHITE_WINS;
    }

    public boolean blackWins() {
        return OUTCOME == Outcome.BLACK_WINS;
    }
}
