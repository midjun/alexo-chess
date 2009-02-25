package ao.heavy;

import model.Board;
import ao.heavy.HeavyNode;
import ao.Bot;

/**
 *
 */
public class HeavyUctBot implements Bot
{
    //--------------------------------------------------------------------
    private final int numRuns;


    //--------------------------------------------------------------------
    public HeavyUctBot()
    {
        this( 128 );
    }
    public HeavyUctBot(int numRuns)
    {
        this.numRuns = numRuns;
    }


    //--------------------------------------------------------------------
    public int act(Board board)
    {
        HeavyNode root = new HeavyNode(board);

        for (int run = 0; run < numRuns; run++)
        {
//            try
//            {
                root.strategize();
//            }
//            catch (Throwable t)
//            {
//                Io.display( t );
//            }
//            if (run % 10 == 0)
//            {
//                Io.display("run: " + run);
                System.out.println("run: " + run + " @ " + root.size());
//            }
        }

        return root.optimize();
    }
}
