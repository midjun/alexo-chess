package ao.chess.v2.engine.heuristic.train;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.heuristic.impl.classification.ClassByMove;
import ao.chess.v2.engine.heuristic.impl.simple.SimpleWinTally;
import ao.chess.v2.engine.heuristic.player.HeuristicPlayer;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.time.Stopwatch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 12:56:20 PM
 */
public class HeuristicTrainer
{
    //--------------------------------------------------------------------
    private static final int TIME_PER_MOVE = 1000;


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        String        id        = "test";
        MoveHeuristic heuristic = SimpleWinTally.retrieve(id);
//        MoveHeuristic heuristic = DoubleWinTally.retrieve(id);
//        MoveHeuristic heuristic = ClassByMove.retrieve(id);
        Stopwatch     timer     = new Stopwatch();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            trainingRound( heuristic );

            if ((i + 1) % 10000 == 0) {
                System.out.println((i + 1) + " took " + timer);
                timer = new Stopwatch();

                heuristic.persist();
            }
        }
    }


    //--------------------------------------------------------------------
    public static void trainingRound(
            MoveHeuristic heuristic)
    {
        List<State> stateHistory  = new ArrayList<State>();
        IntList     actionHistory = new IntArrayList();

        Outcome outcome = round(
                new HeuristicPlayer(heuristic),
                new HeuristicPlayer(heuristic),
                stateHistory, actionHistory);

        for (int i = 0; i < stateHistory.size(); i++) {
            heuristic.update(
                    stateHistory .get( i ),
                    actionHistory.get( i ),
                    outcome);
        }
    }


    //--------------------------------------------------------------------
    private static Outcome round(
            Player white, Player black,
            List<State> stateHistory, IntList actionHistory)
    {
        State state = State.initial();

        while (! state.isDrawnBy50MovesRule())
        {
            Player nextToAct =
                    (state.nextToAct() == Colour.WHITE)
                    ? white : black;

            boolean moveMade = false;
            int move = nextToAct.move(state.prototype(),
                    TIME_PER_MOVE, TIME_PER_MOVE, TIME_PER_MOVE);
            int undoable = -1;
            if (move != -1) {
                State beforeMove = state.prototype();
                undoable = Move.apply(move, state);
                if (undoable != -1) {
                    moveMade = true;

                    stateHistory .add( beforeMove );
                    actionHistory.add( undoable   );
                }
            }

            if (! moveMade) {
                if (state.isInCheck(Colour.WHITE)) {
                    return Outcome.BLACK_WINS;
                } else if (state.isInCheck(Colour.BLACK)) {
                    return Outcome.WHITE_WINS;
                }
                return Outcome.DRAW;
            }
        }
        return Outcome.DRAW;
    }
}
