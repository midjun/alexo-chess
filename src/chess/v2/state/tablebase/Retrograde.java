package ao.chess.v2.state.tablebase;

import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.misc.Traverser;
import it.unimi.dsi.fastutil.longs.*;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 8:30:02 PM
 */
public class Retrograde
        implements Traverser<State>
{
    //--------------------------------------------------------------------
    private final Long2ObjectMap<LongSet> precedents =
            new Long2ObjectOpenHashMap<LongSet>();


    //--------------------------------------------------------------------
    @Override public void traverse(State state)
    {
        int[] moves = state.legalMoves();
        if (moves == null || moves.length == 0) return;

        long parentHash = state.staticHashCode();
        for (int legalMove : moves) {
            Move.apply(legalMove, state);

            getOrCreate( state.staticHashCode() )
                    .add( parentHash );

            Move.unApply(legalMove, state);
        }
    }


    //--------------------------------------------------------------------
    private LongSet getOrCreate(long hash) {
        LongSet parents = precedents.get( hash );
        if (parents == null) {
            parents = new LongOpenHashSet();
            precedents.put(hash, parents);
        }
        return parents;
    }


    //--------------------------------------------------------------------
    public LongIterable precedents(State of) {
        return precedents( of.staticHashCode() );
    }

    public LongIterable precedents(long ofStaticHash) {
        LongSet existing = precedents.get( ofStaticHash );
        return existing == null
               ? LongLists.EMPTY_LIST
               : existing;
    }
}
