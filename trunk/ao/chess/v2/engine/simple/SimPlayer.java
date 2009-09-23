package ao.chess.v2.engine.simple;

import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.PlayerImpl;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.state.Status;
import util.Io;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 12:00:56 AM
 */
public class SimPlayer extends PlayerImpl
{
    //--------------------------------------------------------------------
    private final int simCount;


    //--------------------------------------------------------------------
    public SimPlayer(int numberOfSimulations)
    {
        simCount = numberOfSimulations;
    }


    //--------------------------------------------------------------------
    public int move(
            State position)
    {
        int[] moves  = new int[Move.MAX_PER_PLY];
        int   nMoves = position.legalMoves(moves);
        if (nMoves == 0) return -1;

        int      sims        = simCount / nMoves;
        State    state       = position.prototype();
        double[] expectation = new double[ nMoves ];
        for (int m = 0; m < nMoves; m++) {

            int move = Move.apply(moves[m], state); 
            for (int i = 0; i < sims; i++) {
                expectation[m] += simulate(
                        state.prototype(), position.nextToAct());
            }
            Move.unApply(move, state);
        }

        double maxEv      = -1;
        int    maxEvIndex = 0;
        for (int m = 0; m < nMoves; m++) {
            if (expectation[ m ] > maxEv) {
                maxEv      = expectation[ m ];
                maxEvIndex = m;
            }
        }

        Io.display("Playing: " + (maxEv / sims));
        return moves[ maxEvIndex ];
    }


    //--------------------------------------------------------------------
    private double simulate(State state, Colour fromPov)
    {
        Status  status    = null;
        int     nextCount = 0;
        int[]   nextMoves = new int[ 128 ];
        int[]   moves     = new int[ 128 ];
        int     nMoves    = state.moves(moves);
        Outcome outcome   = null;

        do
        {
            int     move;
            boolean madeMove = false;

            int[] moveOrder = MovePicker.pickRandom(nMoves);
            for (int moveIndex : moveOrder)
            {
                move = Move.apply(moves[ moveIndex ], state);

                // generate opponent moves
                nextCount = state.moves(nextMoves);

                if (nextCount < 0) { // if leads to mate
                    Move.unApply(move, state);
                } else {
                    madeMove = true;
                    break;
                }
            }
            if (! madeMove) {
                outcome = state.isInCheck(state.nextToAct())
                          ? Outcome.loses(state.nextToAct())
                          : Outcome.DRAW;
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        while ((status = state.knownStatus()) == Status.IN_PROGRESS);

        if (outcome == null && status != null) {
            outcome = status.toOutcome();
        }

        return outcome == null
               ? Double.NaN
               : outcome.valueFor(fromPov);
    }
}
