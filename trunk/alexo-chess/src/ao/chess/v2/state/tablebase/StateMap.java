package ao.chess.v2.state.tablebase;

import ao.chess.v2.state.Representation;
import ao.chess.v2.state.State;
import ao.util.misc.Traverser;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

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
//    private final Long2ObjectMap<State> byHash =
//            new Long2ObjectOpenHashMap<State>();
    private final Long2ObjectMap<byte[]> byHash =
            new Long2ObjectOpenHashMap<byte[]>();


    //--------------------------------------------------------------------
    @Override public void traverse(State state)
    {
//        State existing = byHash.get(state.staticHashCode());
        State existing = get(state.staticHashCode());
        if (existing == null) {
//            byHash.put(state.staticHashCode(), state);
            put(state.staticHashCode(), state);
        } else if (! existing.equals(state)) {
            System.out.println("StateMap COLLISION FOUND!!!");
            System.out.println(existing);
            System.out.println("vs");
            System.out.println(state);
            
        }
    }


    //--------------------------------------------------------------------
    private void put(long staticHash, State state) {
        byHash.put(staticHash, Representation.packStream(state));
    }

    private State get(long staticHash) {
        byte[] packed = byHash.get(staticHash);
        return packed == null
               ? null
               : Representation.unpackStream(packed);
    }


    //--------------------------------------------------------------------
    public void traverse(Traverser<State> visitor) {
//        for (State state : byHash.values()) {
        for (byte[] packedState : byHash.values()) {
            State state = Representation.unpackStream(packedState);
            visitor.traverse( state );
        }
    }


    //--------------------------------------------------------------------
    public boolean containsState(long staticHash) {
        return byHash.containsKey( staticHash );
    }

    
    //--------------------------------------------------------------------
    public State stateOf(long staticHash) {
//        return byHash.get( staticHash );
        return get( staticHash );
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
//        return byHash.values();
        return new Iterable<State>() {
            @Override public Iterator<State> iterator() {
                final ObjectIterator<byte[]> itr =
                        byHash.values().iterator();
                return new Iterator<State>() {
                    @Override public boolean hasNext() {
                        return itr.hasNext();
                    }

                    @Override public State next() {
                        return Representation.unpackStream( itr.next() );
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }


    //--------------------------------------------------------------------
    public int size() {
        return byHash.size();
    }
}
