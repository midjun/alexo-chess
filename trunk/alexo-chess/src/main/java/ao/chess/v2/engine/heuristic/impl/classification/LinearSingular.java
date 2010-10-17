package ao.chess.v2.engine.heuristic.impl.classification;

import ao.ai.classify.linear.PassiveAggressive;
import ao.ai.ml.model.algo.OnlineBinaryLearner;
import ao.ai.ml.model.algo.OnlineBinaryScoreLearner;
import ao.ai.ml.model.input.RealList;
import ao.ai.ml.model.output.BinaryClass;
import ao.ai.ml.model.output.BinaryScoreClass;
import ao.chess.v1.model.Board;
import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.heuristic.impl.simple.SimpleWinTally;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.persist.PersistentObjects;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;

/**
 * User: AO
 * Date: Oct 16, 2010
 * Time: 8:46:56 PM
 */
public class LinearSingular
        implements MoveHeuristic, Serializable
{
    //--------------------------------------------------------------------
    private static final long serialVersionUID = 2010 * 10 * 16;

    private static final Logger LOG =
            Logger.getLogger(LinearSingular.class);

    private static final File dir = Config.dir(
            "lookup/heuristic/linear-pa");


    //------------------------------------------------------------------------
    private final String                             id;
    private final OnlineBinaryScoreLearner<RealList> learner;


    //------------------------------------------------------------------------
    public LinearSingular(String id)
    {
        this.id = id;

        OnlineBinaryScoreLearner<RealList> rememberedLearner =
                PersistentObjects.retrieve( new File(dir, id) );

        learner = ((rememberedLearner == null)
                   ? new PassiveAggressive()
                   : rememberedLearner);
    }


    //------------------------------------------------------------------------
    @Override
    public double evaluate(State state, int move)
    {
        BinaryScoreClass classification =
                learner.classify(
                        encode( state ));

        return (state.nextToAct() == Colour.WHITE ? 1 : -1) *
                classification.positiveScore();
    }


    //------------------------------------------------------------------------
    @Override
    public void update(State fromState, int move, Outcome outcome)
    {
        if (outcome == Outcome.DRAW)
        {
            // binary classification for now
            return;
        }

        learner.learn(
                encode( fromState ),
                BinaryClass.create(
                        outcome == Outcome.WHITE_WINS));
    }


    //------------------------------------------------------------------------
    @Override
    public void persist()
    {
        LOG.debug("persisting " + id + " with " + learner);
        PersistentObjects.persist(learner, new File(dir, id));
    }


    //------------------------------------------------------------------------
    private RealList encode(State state)
    {
        double[] coding = new double[ 8 * 8 + 1 ];

        // bias
        coding[ 64 ] = 1;

        int boardIndex = 0;
        for (int rank = 0; rank < 8; rank++)
        {
            for (int file = 0; file < 8; file++)
            {
                Piece piece = state.pieceAt(rank, file);
                coding[ boardIndex++ ] = encode( piece );
            }
        }

        return new RealList( coding );
    }

    private double encode( Piece piece )
    {
        // empty
        if (piece == null) {
            return 0;
        }

        switch (piece)
        {
            case WHITE_PAWN: return  1.00;
            case BLACK_PAWN: return -1.00;

            case WHITE_KNIGHT: return  3.00;
            case BLACK_KNIGHT: return -3.00;

            case WHITE_BISHOP: return  4.00;
            case BLACK_BISHOP: return -4.00;

            case WHITE_ROOK: return  5.00;
            case BLACK_ROOK: return -5.00;

            case WHITE_QUEEN: return  9.00;
            case BLACK_QUEEN: return -9.00;

            case WHITE_KING: return  25.00;
            case BLACK_KING: return -25.00;
        }

        throw new IllegalStateException(
                "Can't recognize piece: " + piece);
    }


    //------------------------------------------------------------------------
    @Override
    public String toString()
    {
        return learner.toString();
    }
}
