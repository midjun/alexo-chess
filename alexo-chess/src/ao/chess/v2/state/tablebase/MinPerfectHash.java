package ao.chess.v2.state.tablebase;

import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.Serializable;
import java.util.List;

/**
 * User: alex
 * Date: 18-Oct-2009
 * Time: 12:04:50 PM
 */
public class MinPerfectHash implements Serializable
{
    //--------------------------------------------------------------------
    private final MinimalPerfectHashFunction<String> hash;


    //--------------------------------------------------------------------
    public MinPerfectHash(List<Piece> material)
    {
        MinimalHashBuilder minHash = new MinimalHashBuilder();

        new PositionTraverser().traverse(
                material, minHash);

        hash = minHash.hash();
    }


    //--------------------------------------------------------------------
    public long index(long staticHash) {
        return hash.getLong(
                MinimalHashBuilder.encode(
                        staticHash));
    }

    public long index(State state) {
        return index( state.staticHashCode() );
    }

    public int size() {
        return hash.size();
    }
}
