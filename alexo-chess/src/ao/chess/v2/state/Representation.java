package ao.chess.v2.state;

import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.piece.Colour;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;

/**
 * User: alex
 * Date: 17-Oct-2009
 * Time: 1:54:47 PM
 *
 * See http://en.wikipedia.org/wiki/Board_representation_(chess)
 */
public class Representation
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        State test = new State(
                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 13 2");

        System.out.println(test);

        byte[] packed = packStream(test);
        State unpacked = unpackStream(packed);
        System.out.println(unpacked);        
    }


    //--------------------------------------------------------------------
    private Representation() {}


    //--------------------------------------------------------------------
    public static State unpackStream(byte[] stream)
    {
        Colour nextToAct =
                ((stream[0] >>> 7) > 0 ? Colour.WHITE : Colour.BLACK);

        byte reversibleMoves = (byte) (stream[0] & 0x7F);

        CastleType.Set castles =
                new CastleType.Set((byte)(stream[1] >>> 4));

        byte enPassantFile = (byte)(stream[1] & 0xF);
        if (enPassantFile > 7) {
            enPassantFile = -1;
        }

        int       nextLocation = 0;
        Piece[][] board        = new Piece[8][8];
        for (int i = 2; i < stream.length; i++) {
            if (stream[i] < 0) {
                nextLocation -= stream[i];
            } else {
                board[ Location.rankIndex(nextLocation) ]
                     [ Location.fileIndex(nextLocation) ] =
                         Piece.VALUES[ stream[i] ];

                nextLocation++;
            }
        }

        return new State(fen(board,
                nextToAct, reversibleMoves, castles, enPassantFile));
    }


    //--------------------------------------------------------------------
    public static byte[] packStream(State state)
    {
        ByteList packed = new ByteArrayList();

        // next to act (1)
        // reversible moves (7)
        int nextToAct = (state.nextToAct() == Colour.WHITE ? 1 : 0);
        packed.add((byte)(nextToAct << 7 |
                          state.reversibleMoves()));

        // castles (4)
        // en passant (4)
        packed.add((byte)(state.castlesAvailable().toBits() << 4 |
                          state.enPassantFile() & 0xF));

//        ByteList board = new ByteArrayList();
        byte empties = 0;
        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                Piece p = state.pieceAt(rank, file);
                if (p == null) {
                    empties++;
                } else {
                    if (empties > 0) {
                        packed.add( (byte) -empties );
                    }
                    packed.add((byte) p.ordinal());
                    empties = 0;
                }
            }
        }


//        long     emp
//        ByteList nibbleBoard = new ByteArrayList();
//        for (byte squareInfo : board) {
//            if (squareInfo > 0) {
//                nibbleBoard.add( squareInfo );
//            } else {
//                int empty = -squareInfo;
//                while (empty > 0) {
//                    byte emptySpan = (byte) Math.min(empty, 16);
//                    nibbleBoard.add( emptySpan );
//                    empty -= emptySpan;
//                }
//            }
//        }

//        for (byte pieceInfo : board) {
//            if (emptiesNext) {
//
//                emptiesNext = false;
//            } else {
//                int piece = pieceInfo
//            }
//        }


        return packed.toByteArray();
    }


    //--------------------------------------------------------------------
    public static String fen(
            Piece[][]      board,
            Colour         nextToAct,
            int            reversibleMoves,
            CastleType.Set castles,
            int            enPassant)
    {
        StringBuilder str = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < 8; file++) {
                Piece p = board[rank][file];
                if (p == null) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        str.append(emptySquares);
                        emptySquares = 0;
                    }
                    str.append(p.toString());
                }
            }
            if (emptySquares > 0) {
                str.append(emptySquares);
            }

            if (rank != 0 ) {
                str.append("/");
            }
        }

        str.append(" ");
        str.append(nextToAct == Colour.WHITE
                   ? "w" : "b");
        str.append(" ");

        // castles
//        str.append("-");
        str.append(castles.toFen());

        str.append(" ");

		// En passant square
        if (enPassant == -1) {
            str.append("-");
        } else {
            str.append(State.FILES.charAt(enPassant));
//            str.append(" ");
            if (nextToAct == Colour.WHITE) {
                str.append(6);
            } else {
                str.append(3);
            }
        }
        str.append(" ");

        // reversible moves
//        str.append("0");
        str.append(reversibleMoves);

        str.append(" ");

        // full moves since start of game
        str.append("n");

		return str.toString();
    }
}
