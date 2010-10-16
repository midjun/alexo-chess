package ao.chess.v2.state.tablebase;

import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.misc.Traverser;
import it.unimi.dsi.bits.HuTuckerTransformationStrategy;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 12:53:37 AM
 */
public class MinimalHash
    implements Traverser<State>,
               Iterable<String>
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        MinimalHash minHash = new MinimalHash();

        new PositionTraverser().traverse(
                Arrays.asList(
                        Piece.WHITE_KING,
                        Piece.BLACK_KING),
                minHash);

        minHash.hash();
        minHash.displayReport();
    }


    //--------------------------------------------------------------------
    private final LongSet states =
            new LongOpenHashSet();

    private long count = 0;


    //--------------------------------------------------------------------
    @Override public void traverse(State state) {
        states.add( state.longHashCode() );
        count++;
    }


    //--------------------------------------------------------------------
    public MinimalPerfectHashFunction<String> hash()
    {
        try {
            return new MinimalPerfectHashFunction<String>(
                            this, new HuTuckerTransformationStrategy(
                                        this, true));
        } catch (IOException e) {
            throw new Error( e );
        }
    }


    //--------------------------------------------------------------------
    @Override public Iterator<String> iterator() {
        final LongIterator itr = states.iterator();
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public String next() {
                return String.valueOf(itr.next());
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    //--------------------------------------------------------------------
    private void displayReport() {
        System.out.println("count\t" + count);
    }
}