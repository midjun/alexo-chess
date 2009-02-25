package v2.state;

import v2.data.BitBoard;
import v2.data.BitLoc;
import v2.piece.Colour;
import v2.piece.Figure;
import v2.piece.Piece;

/**
 * Date: Feb 6, 2009
 * Time: 2:07:25 AM
 */
public class State
{
    //--------------------------------------------------------------------
    private static final byte WHITE_K_CASTLE = 1;
    private static final byte WHITE_Q_CASTLE = 1 << 1;
    private static final byte BLACK_K_CASTLE = 1 << 2;
    private static final byte BLACK_Q_CASTLE = 1 << 3;

    private static final byte WHITE_CASTLE = WHITE_K_CASTLE |
                                             WHITE_Q_CASTLE;
    private static final byte BLACK_CASTLE = BLACK_K_CASTLE |
                                             BLACK_Q_CASTLE;

    private static final byte BLACK_CASTLE_SHIFT = 2;

    private static final int WHITE_PAWNS   = Piece.WHITE_PAWN.ordinal();
    private static final int BLACK_PAWNS   = Piece.BLACK_PAWN.ordinal();
    private static final int WHITE_KNIGHTS = Piece.WHITE_KNIGHT.ordinal();
    private static final int BLACK_KNIGHTS = Piece.BLACK_KNIGHT.ordinal();
    private static final int WHITE_BISHOPS = Piece.WHITE_BISHOP.ordinal();
    private static final int BLACK_BISHOPS = Piece.BLACK_BISHOP.ordinal();
    private static final int WHITE_ROOKS   = Piece.WHITE_ROOK.ordinal();
    private static final int BLACK_ROOKS   = Piece.BLACK_ROOK.ordinal();
    private static final int WHITE_QUEENS  = Piece.WHITE_QUEEN.ordinal();
    private static final int BLACK_QUEENS  = Piece.BLACK_QUEEN.ordinal();
    private static final int WHITE_KING    = Piece.WHITE_KING.ordinal();
    private static final int BLACK_KING    = Piece.BLACK_KING.ordinal();


    //--------------------------------------------------------------------
    private long[] pieces;

    private long whitePieces;
    private long blackPieces;

    private byte whiteEnPassants; // En Passant available
    private byte blackEnPassants; // En Passant available

    private byte castles;
    private byte reversibleMoves;

    private Colour nextToAct;


    //--------------------------------------------------------------------
    public State()
    {
        pieces = new long[ Piece.VALUES.length ];

        for (int p = 0; p < 8; p++) {
            pieces[ WHITE_PAWNS ] |= BitLoc.locationToBitBoard(1, p);
            pieces[ BLACK_PAWNS ] |= BitLoc.locationToBitBoard(6, p);
        }

        pieces[ WHITE_ROOKS ] = BitLoc.locationToBitBoard(0, 0) |
                                BitLoc.locationToBitBoard(0, 7);
        pieces[ BLACK_ROOKS ] = BitLoc.locationToBitBoard(7, 0) |
                                BitLoc.locationToBitBoard(7, 7);

        pieces[ WHITE_KNIGHTS ] = BitLoc.locationToBitBoard(0, 1) |
                                  BitLoc.locationToBitBoard(0, 6);
        pieces[ BLACK_KNIGHTS ] = BitLoc.locationToBitBoard(7, 1) |
                                  BitLoc.locationToBitBoard(7, 6);

        pieces[ WHITE_BISHOPS ] = BitLoc.locationToBitBoard(0, 2) |
                                  BitLoc.locationToBitBoard(0, 5);
        pieces[ BLACK_BISHOPS ] = BitLoc.locationToBitBoard(7, 2) |
                                  BitLoc.locationToBitBoard(7, 5);

        pieces[ WHITE_QUEENS ] = BitLoc.locationToBitBoard(0, 3);
        pieces[ BLACK_QUEENS ] = BitLoc.locationToBitBoard(7, 3);

        pieces[ WHITE_KING ] = BitLoc.locationToBitBoard(0, 4);
        pieces[ BLACK_KING ] = BitLoc.locationToBitBoard(7, 4);

        whiteEnPassants = 0;
        blackEnPassants = 0;

        castles = WHITE_K_CASTLE |
                  WHITE_Q_CASTLE |
                  BLACK_K_CASTLE |
                  BLACK_Q_CASTLE;

        reversibleMoves = 0;
        nextToAct       = Colour.WHITE;

        for (Piece p : Piece.VALUES) {
            if (p.isWhite()) {
                whitePieces |= pieces[ p.ordinal() ];
            } else {
                blackPieces |= pieces[ p.ordinal() ];
            }
        }
    }


    private State(long[] copyPieces,
                  byte   copyWhiteEnPassants,
                  byte   copyBlackEnPassants,
                  byte   copyCastles,
                  byte   copyReversibleMoves,
                  Colour copyNextToAct,
                  long   copyWhitePieces,
                  long   copyBlackPieces)
    {
        pieces = copyPieces;

        whiteEnPassants = copyWhiteEnPassants;
        blackEnPassants = copyBlackEnPassants;

        castles         = copyCastles;
        reversibleMoves = copyReversibleMoves;
        nextToAct       = copyNextToAct;

        whitePieces = copyWhitePieces;
        blackPieces = copyBlackPieces;
    }


    //--------------------------------------------------------------------
    private byte decodeCastles(int rights, Colour forSide)
    {
        return (byte)((forSide == Colour.WHITE)
               ? castles & BLACK_CASTLE | rights
               : castles & WHITE_CASTLE |
                 rights << BLACK_CASTLE_SHIFT);
    }

    private byte encodeCastles(Colour forSide)
    {
        return (byte) ((forSide == Colour.WHITE)
               ? castles & WHITE_CASTLE
               : (castles & BLACK_CASTLE) >> BLACK_CASTLE_SHIFT);
    }


    //--------------------------------------------------------------------
    /**
     * generate all legal moves from this position
     *
     * @param moves generate moves into
     * @return number of moves generated
     */
    public int moves(int[] moves)
    {
        long occupied      = whitePieces | blackPieces;
        long notOccupied   = ~occupied;

        long proponent, opponent, oppKing;
        if (nextToAct == Colour.WHITE) {
            proponent = whitePieces;
            opponent  = blackPieces;
            oppKing   = pieces[ Piece.BLACK_KING.ordinal() ];
        } else {
            proponent = blackPieces;
            opponent  = whitePieces;
            oppKing   = pieces[ Piece.WHITE_KING.ordinal() ];
        }
        long notProponent = ~proponent;
        long notOpponent  = ~opponent;

        int offset = 0;
        for (Figure f : Figure.VALUES)
        {
            Piece p  = Piece.valueOf(nextToAct, f);
            long  bb = pieces[ p.ordinal() ];
            while (bb != 0)
            {
                long pieceBoard  = BitBoard.lowestOneBit(bb);
                long pseudoMoves = p.moves(
                        pieceBoard, occupied, notOccupied,
                        proponent, notProponent, opponent);
                if ((oppKing & pseudoMoves) != 0) return -1;

//                prevActGenMoves |= pseudoMoves;

                offset = addMoves(
                        p, pieceBoard, moves, offset,
                        pseudoMoves, opponent, notOpponent);

                // reset LS1B
                bb &= bb - 1;
            }
        }
        return offset;
    }
    
    private int addMoves(
            Piece piece,
            long  fromBB,
            int[] moves,
            int   offset,
            long  movesBB,
            long  opponent,
            long  notOpponent)
    {
        int from = BitLoc.bitBoardToLocation(fromBB);
        int off = addMobility(
                piece, from, moves, offset, movesBB & notOpponent);
        return off;
//        return addCaptures(
//                piece, from, moves, off, movesBB & opponent);
    }

    private int addMobility(
            Piece piece,
            int   from,
            int[] moves,
            int   offset,
            long  moveBB)
    {
        if (moveBB == 0) return offset;
        while (moveBB != 0)
        {
            long moveBoard = BitBoard.lowestOneBit(moveBB);
            moves[ offset++ ] = Move.mobility(
                    piece, from, BitLoc.bitBoardToLocation(moveBoard),
                    encodeCastles(piece.colour()), reversibleMoves);
            moveBB &= moveBB - 1;
        }
        return offset;
    }

    private int addCaptures(
            Piece piece,
            int   from,
            int[] moves,
            int   offset,
            long  moveBB)
    {
        if (moveBB == 0) return offset;
        while (moveBB != 0)
        {
            long moveBoard = BitBoard.lowestOneBit(moveBB);
            moves[ offset++ ] = Move.capture(
                    piece, from, BitLoc.bitBoardToLocation(moveBoard),
                    encodeCastles(piece.colour()), reversibleMoves);
            moveBB &= moveBB - 1;
        }
        return offset;
    }


    //--------------------------------------------------------------------
    public void unMobalize(
            Piece piece,
            int   fromSquareIndex,
            int   toSquareIndex,
            int   castlingRights,
            byte  reversibles)
    {
        mobalize(piece, toSquareIndex, fromSquareIndex, false);
        castles         = decodeCastles(castlingRights, piece.colour());
        reversibleMoves = reversibles;
    }


    //--------------------------------------------------------------------
    public void mobalize(
            Piece   piece,
            int     fromSquareIndex,
            int     toSquareIndex)
    {
        mobalize(piece, fromSquareIndex, toSquareIndex, true);
    }
    private void mobalize(
            Piece   piece,
            int     fromSquareIndex,
            int     toSquareIndex,
            boolean conciderCaslingAndReversibles)
    {
        mobalize(piece,
                 BitLoc.locationToBitBoard(fromSquareIndex),
                 BitLoc.locationToBitBoard(toSquareIndex));

        if (conciderCaslingAndReversibles) {
            updateCasltingRights(
                    piece.figure(), fromSquareIndex);

            if (piece.figure() == Figure.PAWN) {
                reversibleMoves = 0;
            } else {
                reversibleMoves++;
            }
        }
    }
    private void mobalize(
            Piece piece,
            long  from,
            long  to)
    {
        long fromTo = from ^ to;
        pieces[ piece.ordinal() ] ^= fromTo;

        // update white or black color bitboard
        if (piece.colour() == Colour.WHITE) whitePieces ^=  fromTo;
        else                                blackPieces ^=  fromTo;

        nextToAct = nextToAct.invert();
    }

    private void updateCasltingRights(
            Figure mover, int from)
    {
        if (castles == 0) return;
        if (mover == Figure.KING) {
            castles &= ~((nextToAct == Colour.WHITE)
                         ? WHITE_CASTLE : BLACK_CASTLE);
        } else if (mover == Figure.ROOK) {
            if (nextToAct == Colour.WHITE) {
                if (from == 0) {
                    castles &= ~WHITE_Q_CASTLE;
                } else if (from == 7) {
                    castles &= ~WHITE_K_CASTLE;
                }
            } else {
                if (from == 56) {
                    castles &= ~BLACK_Q_CASTLE;
                } else if (from == 7) {
                    castles &= ~BLACK_K_CASTLE;
                }
            }
        }
    }


    //--------------------------------------------------------------------
    public void unCapture(
            Piece attacker,
            Piece captured,
            int   fromSquareIndex,
            int   toSquareIndex,
            int   availCastles,
            byte  reversibles)
    {
        long from   = BitLoc.locationToBitBoard(fromSquareIndex);
        long to     = BitLoc.locationToBitBoard(  toSquareIndex);
        long fromTo = from ^ to;

        pieces[ attacker.ordinal() ] ^= fromTo;
        pieces[ captured.ordinal() ] ^= to;     //reset the captured piece

        if (nextToAct == Colour.WHITE) {
            // black did the capture
            blackPieces ^= fromTo;
            whitePieces ^= to;
        } else {
            whitePieces ^= fromTo;
            blackPieces ^= to;
        }

        nextToAct       = nextToAct.invert();
        castles         = decodeCastles(availCastles, attacker.colour());
        reversibleMoves = reversibles;
    }


    //--------------------------------------------------------------------
    public Figure capture(
            Piece   attacker,
            int     fromSquareIndex,
            int     toSquareIndex)
    {
        long toBB = BitLoc.locationToBitBoard(toSquareIndex);
        Piece captured =
                pieceAt(toBB, attacker.colour().invert());
        capture(attacker.ordinal(),
                captured.ordinal(),
                BitLoc.locationToBitBoard(fromSquareIndex),
                BitLoc.locationToBitBoard(toSquareIndex));

        updateCasltingRights(
                attacker.figure(), fromSquareIndex);
        reversibleMoves = 0;
        return captured.figure();
    }
    private void capture(
            int  attacker,
            int  captured,
            long from,
            long to)
    {
        long fromTo = from ^ to; // |+

        pieces[attacker] ^= fromTo;   // update piece bitboard
        pieces[captured] ^= to;       // reset the captured piece

        if (nextToAct == Colour.WHITE) {
            whitePieces ^= fromTo;
            blackPieces ^= to;
        } else {
            blackPieces ^= fromTo;
            whitePieces ^= to;
        }

        nextToAct = nextToAct.invert();
    }


    //--------------------------------------------------------------------
    public boolean isInCheck(Colour colour)
    {
        Colour prop          = colour.invert();
        long   occupied      = whitePieces | blackPieces;
        long   notOccupied   = ~occupied;

        long proponent, opponent, oppKing;
        if (prop == Colour.WHITE) {
            proponent = whitePieces;
            opponent  = blackPieces;
            oppKing   = pieces[ BLACK_KING ];
        } else {
            proponent = blackPieces;
            opponent  = whitePieces;
            oppKing   = pieces[ WHITE_KING ];
        }
        long notProponent = ~proponent;

        for (Figure f : Figure.VALUES)
        {
            Piece p  = Piece.valueOf(prop, f);
            long  bb = pieces[ p.ordinal() ];
            while (bb != 0)
            {
                long pieceBoard  = BitBoard.lowestOneBit(bb);
                long pseudoMoves = p.moves(
                        pieceBoard, occupied, notOccupied,
                        proponent, notProponent, opponent);
                if ((oppKing & pseudoMoves) != 0) return true;
                bb &= bb - 1;
            }
        }
        return false;
    }

    public Colour nextToAct()
    {
        return nextToAct;
    }


    //--------------------------------------------------------------------
    // see http://chessprogramming.wikispaces.com/Draw+evaluation
    // can later be substituted with tablebase
    public Status knownStatus()
    {
        if (reversibleMoves > 100) return Status.DRAW;

        // no major pieces
        if (pieces[ WHITE_ROOKS  ] != 0 ||
            pieces[ BLACK_ROOKS  ] != 0 ||
            pieces[ WHITE_QUEENS ] != 0 ||
            pieces[ BLACK_QUEENS ] != 0) return Status.IN_PROGRESS;

        boolean whiteBishops, blackBishops;
        boolean whiteKnights, blackKnights;

        boolean whitePawns = (pieces[ WHITE_PAWNS ] != 0);
        boolean blackPawns = (pieces[ BLACK_PAWNS ] != 0);
        if (whitePawns && blackPawns) {
            return Status.IN_PROGRESS;
        } else {
            whiteBishops = (pieces[ WHITE_BISHOPS ] != 0);
            blackBishops = (pieces[ BLACK_BISHOPS ] != 0);

            whiteKnights = (pieces[ WHITE_KNIGHTS ] != 0);
            blackKnights = (pieces[ BLACK_KNIGHTS ] != 0);

            if (whitePawns || blackPawns) {
                // at least one side has at least a minor pawn
                if (whiteBishops || blackBishops ||
                    whiteKnights || blackKnights) {
                    return Status.IN_PROGRESS;
                } else {
                    if (whitePawns) {
                        int nWhitePawns =
                                Long.bitCount(pieces[ WHITE_PAWNS ]);
                        return (nWhitePawns == 1)
                               ? Status.DRAW : Status.IN_PROGRESS;
                    } else {
                        int nBlackPawns =
                                Long.bitCount(pieces[ BLACK_PAWNS ]);
                        return (nBlackPawns == 1)
                               ? Status.DRAW : Status.IN_PROGRESS;
                    }
                }
            }
        }
        // no pawns

        if (whiteBishops && blackBishops) {
            if (whiteKnights || blackKnights){
                return Status.IN_PROGRESS;
            }

            // both sides have a king and a bishop,
            //   the bishops being the same color
            int nWhiteBishops = Long.bitCount(pieces[ WHITE_BISHOPS ]);
            if (nWhiteBishops > 1) return Status.IN_PROGRESS;

            int nBlackBishops = Long.bitCount(pieces[ WHITE_BISHOPS ]);
            if (nBlackBishops > 1) return Status.IN_PROGRESS;

            return (BitBoard.isDark(pieces[ WHITE_BISHOPS ]) ==
                    BitBoard.isDark(pieces[ BLACK_BISHOPS ]))
                   ? Status.DRAW : Status.IN_PROGRESS;
        }
        else if (whiteBishops || blackBishops)
        {
            // one player has a bishop
            return (whiteKnights || blackKnights)
                   ? Status.IN_PROGRESS
                   : Status.DRAW;
        }
        // no bishops

        if (whiteKnights && blackKnights) return Status.IN_PROGRESS;
        if (whiteKnights) {
            int nWhiteKnights =
                    Long.bitCount(pieces[ WHITE_KNIGHTS ]);

            //one side has two knights against the bare king
            return (nWhiteKnights <= 2)
                    ? Status.DRAW : Status.IN_PROGRESS;
        } else if (blackKnights) {
            int nBlackKnights =
                    Long.bitCount(pieces[ BLACK_KNIGHTS ]);
            return (nBlackKnights <= 2)
                    ? Status.DRAW : Status.IN_PROGRESS;
        }
        return Status.DRAW;
    }


    //--------------------------------------------------------------------
    private Piece pieceAt(int rankIndex, int fileIndex)
    {
        long loc = BitLoc.locationToBitBoard(rankIndex, fileIndex);
        for (Piece p : Piece.VALUES) {
            if ((pieces[ p.ordinal() ] & loc) != 0) return p;
        }
        return null;
    }

    private Piece pieceAt(long location, Colour ofColour)
    {
        for (Figure f : Figure.VALUES)
        {
            Piece p        = Piece.valueOf(ofColour, f);
            long  occupied = pieces[ p.ordinal() ];
            if ((occupied & location) != 0) return p;
        }
        return null;
    }


    //--------------------------------------------------------------------
    public State prototype()
    {
        return new State(pieces.clone(),
                         whiteEnPassants, blackEnPassants,
                         castles,         reversibleMoves,
                         nextToAct,
                         whitePieces,     blackPieces);
    }


    public boolean checkPieces()
    {
        return whitePieces == calcPieces(Colour.WHITE) &&
               blackPieces == calcPieces(Colour.BLACK); 
    }
    public long calcPieces(Colour c)
    {
        long bb = 0;
        for (Figure f : Figure.VALUES)
        {
            bb |= pieces[ Piece.valueOf(c, f).ordinal() ];
        }
        return bb;
    }


    //--------------------------------------------------------------------
    public String toString() {
        StringBuffer str = new StringBuffer();

        str.append("Reversible Moves: ").append(reversibleMoves);

        str.append("\nCastles Available: ");
        if (castles == 0) {
            str.append("none");
        } else {
            if ((castles & WHITE_CASTLE) != 0) {
                str.append("[white: ");
                if ((castles & WHITE_CASTLE) == WHITE_CASTLE) {
                    str.append("O-O, O-O-O");
                } else if ((castles & WHITE_Q_CASTLE) != 0) {
                    str.append("O-O-O");
                } else {
                    str.append("O-O");
                }
                str.append("] ");
            }
            if ((castles & BLACK_CASTLE) != 0) {
                str.append("[black: ");
                if ((castles & BLACK_CASTLE) == BLACK_CASTLE) {
                    str.append("O-O, O-O-O");
                } else if ((castles & BLACK_Q_CASTLE) != 0) {
                    str.append("O-O-O");
                } else {
                    str.append("O-O");
                }
                str.append("]");
            }
        }

        str.append("\nEn Passants: ");
        if (whiteEnPassants == 0 && blackEnPassants == 0) {
            str.append("none");
        } else {
            if (whiteEnPassants != 0) {
                str.append("white ")
                   .append(Long.lowestOneBit(whiteEnPassants))
                   .append(" ");
            }
            if (blackEnPassants != 0) {
                str.append("black ")
                   .append(Long.lowestOneBit(blackEnPassants));
            }
        }

        for (int rank = 7; rank >= 0; rank--)
        {
            str.append("\n");
            for (int file = 0; file < 8; file++)
            {
                Piece p = pieceAt(rank, file);
                str.append((p == null) ? " " : p);
            }
        }

        return str.toString();
    }
}
