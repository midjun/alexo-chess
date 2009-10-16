package ao.chess.v2.state.tablebase;

import ao.chess.v2.state.State;
import ao.util.misc.Traverser;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.Iterator;

/**
 * User: aostrovsky
 * Date: 15-Oct-2009
 * Time: 3:58:41 PM
 */
public class StateMap
        implements Traverser<State>
{
    //--------------------------------------------------------------------
    private final Long2ObjectMap<State> byHash =
            new Long2ObjectOpenHashMap<State>();


    //--------------------------------------------------------------------
    @Override public void traverse(State state)
    {
        byHash.put(state.staticHashCode(), state);
    }


    //--------------------------------------------------------------------
    public void traverse(Traverser<State> visitor) {
        for (State state : byHash.values()) {
            visitor.traverse( state );
        }
    }


    //--------------------------------------------------------------------
    public boolean containsState(long staticHash) {
        return byHash.containsKey( staticHash );
    }

    
    //--------------------------------------------------------------------
    public State stateOf(long staticHash) {
        return byHash.get( staticHash );
    }


    //--------------------------------------------------------------------
    public Iterable<State> states(LongIterable staticHashes) {
        final LongIterator itr = staticHashes.iterator();
        return new Iterable<State>() {
            @Override public Iterator<State> iterator() {
                return new Iterator<State>() {
                    @Override public boolean hasNext() {
                        return itr.hasNext();
                    }

                    @Override public State next() {
                        return stateOf( itr.nextLong() );
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }


    //--------------------------------------------------------------------
    public Iterable<State> states() {
        return byHash.values();
    }


    //--------------------------------------------------------------------
    public int size() {
        return byHash.size();
    }
}
