package ao.chess.v2.state.tablebase;

import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.data.Arr;
import ao.util.data.AutovivifiedList;
import ao.util.math.stats.Permuter;
import ao.util.misc.Factories;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 10:01:26 PM
 */
public class Oracle implements Serializable
{
    //--------------------------------------------------------------------


    //--------------------------------------------------------------------
    public static void main(String[] args) {
        Oracle oracle = new Oracle(3);
    }


    //--------------------------------------------------------------------
    private final Int2ObjectMap<MaterialOracle> oracles =
            new Int2ObjectOpenHashMap<MaterialOracle>();

    private final int                           pieceCount;


    //--------------------------------------------------------------------
    public Oracle(int nPieces)
    {
        pieceCount = nPieces;

        int nNonKings = nPieces - 2;
        for (int n = 1; n <= nNonKings; n++) {
            addPermutations(n);
        }
    }


    //--------------------------------------------------------------------
    private void addPermutations(int n) {
        List<List<Piece[]>> byPawnCount =
                new AutovivifiedList<List<Piece[]>>(
                        Factories.<Piece[]>newArrayList());

        for (Piece[] permutation :
                new Permuter<Piece>(Piece.VALUES, n)) {
            if (hasKing(permutation)) continue;
            byPawnCount.get(
                    pawnCount(permutation)
            ).add( permutation );
        }
        
        for (List<Piece[]> pieceLists : byPawnCount) {
            for (Piece[] pieces : pieceLists) {
                add( pieces );
            }
        }
    }


    //--------------------------------------------------------------------
    private boolean hasKing(Piece[] pieces) {
        return Arr.indexOf(pieces, Piece.WHITE_KING) != -1 ||
                Arr.indexOf(pieces, Piece.BLACK_KING) != -1;
    }

    private int pawnCount(Piece[] pieces) {
        int count = 0;
        for (Piece p : pieces) {
            if (p.figure() == Figure.PAWN) {
                count++;
            }
        }
        return count;
    }


    //--------------------------------------------------------------------
    private void add(Piece... piece) {
        int tally = MaterialTally.tally(piece);
        if (oracles.containsKey(tally)) return;
        oracles.put(tally, new MaterialOracle(this, nonKings(piece)));
    }

    private List<Piece> nonKings(Piece... pieces) {
        List<Piece> pieceList = new ArrayList<Piece>();
        pieceList.add( Piece.BLACK_KING );
        pieceList.add( Piece.WHITE_KING );
        pieceList.addAll( Arrays.asList(pieces) );
        return pieceList;
    }


    //--------------------------------------------------------------------
    public Outcome see(State position)
    {
        MaterialOracle oracle =
                oracles.get( position.tallyNonKings(pieceCount) );
        return (oracle == null)
               ? null : oracle.see( position.staticHashCode() );
    }
}
