package ao.chess.v2.state.tablebase;

import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.misc.Traverser;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Arrays;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 12:25:59 AM
 */
public class HashCollide
        implements Traverser<State>
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {

        HashCollide visitor = new HashCollide();
        new PositionTraverser().traverse(
                Arrays.asList(
                        Piece.WHITE_KING,
                        Piece.BLACK_KING,
                        Piece.WHITE_PAWN,
                        Piece.WHITE_PAWN),
                visitor);
        visitor.displayReport();
    }


    //--------------------------------------------------------------------
    private final Long2ObjectMap<State> byHash =
            new Long2ObjectOpenHashMap<State>();

    private int count      = 0;
    private int collisions = 0;

    
    //--------------------------------------------------------------------
    @Override public void traverse(State state) {
        long hash = state.longHashCode();

        State existing = byHash.get( hash );
        if (existing == null) {
            byHash.put( hash, state );
        } else if (! existing.equals( state )) {
            System.out.println("collision found for " + state);
            collisions++;
        }
        count++;
    }


    //--------------------------------------------------------------------
    private void displayReport() {
        System.out.println(collisions + " of " + count);
    }
}
