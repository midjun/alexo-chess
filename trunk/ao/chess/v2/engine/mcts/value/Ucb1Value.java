package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsValue;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 10:54:23 PM
 */
public class Ucb1Value implements MctsValue<Ucb1Value>
{
    //--------------------------------------------------------------------
    private int    visits;
    private double sum;


    //--------------------------------------------------------------------
    public Ucb1Value()
    {
        visits = 0;
        sum    = 0;
    }


    //--------------------------------------------------------------------
    private double averageReward() {
        return sum / visits;
    }


    //--------------------------------------------------------------------
    @Override
    public void update(double winRate) {
        visits++;
        sum += winRate;
    }


    //--------------------------------------------------------------------
    @Override
    public double confidenceBound(Ucb1Value withRespectToParent) {
        return averageReward() +
               Math.sqrt((2 * Math.log(withRespectToParent.visits)) /
                         visits);
    }
}
