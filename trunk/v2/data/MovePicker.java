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
    private static final int       nMoves   = 256;
    private static final int       picsPerN = 32;

    private static final int[]     lastPick = new int[ nMoves ];
    private static final int[][][] allPicks = new int[ nMoves ][][];


    //--------------------------------------------------------------------
    public static int[] pick(int nMoves)
    {
        int[][] availPicks = allPicks[ nMoves ];
        if (availPicks == null) {
            availPicks = new int[ picsPerN ][ nMoves ];

            for (int i = 0; i < picsPerN; i++) {
                int[] picks = availPicks[ i ];
                for (int j = 0; j < picks.length; j++) picks[j] = j;
                shuffle(picks);
            }
            allPicks[ nMoves ] = availPicks;
        }
        return availPicks[ lastPick[nMoves]++ % picsPerN ];
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
