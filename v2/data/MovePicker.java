package v2.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: alexo
 * Date: Feb 26, 2009
 * Time: 1:00:52 AM
 */
public class MovePicker
{
    //--------------------------------------------------------------------
    private MovePicker() {}


    //--------------------------------------------------------------------
    private static final int       maxMoves = 128;
    private static final int       picsPerN = 128;

    private static final long[]    lastPick = new long[ maxMoves ];
    private static final int[][][] allPicks = new int[ maxMoves ][][];

    static
    {
        for (int nMoves = 0; nMoves < maxMoves; nMoves++) {
            int[][] availPicks = new int[ picsPerN ][ nMoves ];

            for (int i = 0; i < picsPerN; i++) {
                int[] picks = availPicks[ i ];
                for (int j = 0; j < picks.length; j++) picks[j] = j;
                shuffle(picks);
            }
            allPicks[ nMoves ] = availPicks;
        }
    }


    //--------------------------------------------------------------------
    public static int[] pick(int nMoves)
    {
        return allPicks[ nMoves                               ]
                       [ (int)(lastPick[nMoves]++ % picsPerN) ];
    }


    //--------------------------------------------------------------------
    private static void shuffle(int[] vals)
    {
        List<Integer> l = new ArrayList<Integer>();
        for (int v : vals) l.add( v );
        Collections.shuffle(l);
        for (int i = 0; i < vals.length; i++) {
            vals[ i ] = l.get( i );
        }
    }
}
