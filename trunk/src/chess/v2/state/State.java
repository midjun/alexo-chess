package ao.chess.v2.state;

import ao.chess.v2.data.BitBoard;
import ao.chess.v2.data.BitLoc;
import ao.chess.v2.data.Location;
import ao.chess.v2.move.SlidingPieces;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Date: Feb 6, 2009
 * Time: 2:07:25 AM
 *
 * NOTE: can only undo ONE move, after that the reversible moves and
 *          allowed castles might be out of whack.
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

    private static final long WHITE_KING_START =
            BitLoc.locationToBitBoard(0, 4);
    private static final long BLACK_KING_START =
            BitLoc.locationToBitBoard(7, 4);
    private static final int  WHITE_KING_START_INDEX =
            BitLoc.bitBoardToLocation(WHITE_KING_START);
    private static final int  BLACK_KING_START_INDEX =
            BitLoc.bitBoardToLocation(BLACK_KING_START);

    private static final long WHITE_K_CASTLE_PATH = WHITE_KING_START |
            SlidingPieces.castFiles(WHITE_KING_START,  2);
    private static final long WHITE_Q_CASTLE_PATH = WHITE_KING_START |
            SlidingPieces.castFiles(WHITE_KING_START, -2);
    private static final long BLACK_K_CASTLE_PATH = BLACK_KING_START |
            SlidingPieces.castFiles(BLACK_KING_START,  2);
    private static final long BLACK_Q_CASTLE_PATH = BLACK_KING_START |
            SlidingPieces.castFiles(BLACK_KING_START, -2);

    private static final long WHITE_K_CASTLE_CORRIDOR =
            SlidingPieces.castFiles(WHITE_KING_START,  2);
    private static final long WHITE_Q_CASTLE_CORRIDOR =
            SlidingPieces.castFiles(WHITE_KING_START, -3);
    private static final long BLACK_K_CASTLE_CORRIDOR =
            SlidingPieces.castFiles(BLACK_KING_START,  2);
    private static final long BLACK_Q_CASTLE_CORRIDOR =
            SlidingPieces.castFiles(BLACK_KING_START, -3);

    private static final long WHITE_K_CASTLE_END =
            BitLoc.locationToBitBoard(0, 6);
    private static final long WHITE_Q_CASTLE_END =
            BitLoc.locationToBitBoard(0, 2);
    private static final long BLACK_K_CASTLE_END =
            BitLoc.locationToBitBoard(7, 6);
    private static final long BLACK_Q_CASTLE_END =
            BitLoc.locationToBitBoard(7, 2);
    private static final int  WHITE_K_CASTLE_END_INDEX =
            BitLoc.bitBoardToLocation(WHITE_K_CASTLE_END);
    private static final int  WHITE_Q_CASTLE_END_INDEX =
            BitLoc.bitBoardToLocation(WHITE_Q_CASTLE_END);
    private static final int  BLACK_K_CASTLE_END_INDEX =
            BitLoc.bitBoardToLocation(BLACK_K_CASTLE_END);
    private static final int  BLACK_Q_CASTLE_END_INDEX =
            BitLoc.bitBoardToLocation(BLACK_Q_CASTLE_END);

    private static final long WHITE_K_CASTLE_MOVE =
            WHITE_KING_START ^ WHITE_K_CASTLE_END;
    private static final long WHITE_Q_CASTLE_MOVE =
            WHITE_KING_START ^ WHITE_Q_CASTLE_END;
    private static final long BLACK_K_CASTLE_MOVE =
            BLACK_KING_START ^ BLACK_K_CASTLE_END;
    private static final long BLACK_Q_CASTLE_MOVE =
            BLACK_KING_START ^ BLACK_Q_CASTLE_END;

    private static final long WHITE_K_ROOK_START =
            BitLoc.locationToBitBoard(0, 7);
    private static final long WHITE_Q_ROOK_START =
            BitLoc.locationToBitBoard(0, 0);
    private static final long BLACK_K_ROOK_START =
            BitLoc.locationToBitBoard(7, 7);
    private static final long BLACK_Q_ROOK_START =
            BitLoc.locationToBitBoard(7, 0);

    private static final long WHITE_K_CASTLE_ROOK_END =
            BitBoard.offset(WHITE_K_CASTLE_END, 0, -1);
    private static final long WHITE_Q_CASTLE_ROOK_END =
            BitBoard.offset(WHITE_Q_CASTLE_END, 0,  1);
    private static final long BLACK_K_CASTLE_ROOK_END =
            BitBoard.offset(BLACK_K_CASTLE_END, 0, -1);
    private static final long BLACK_Q_CASTLE_ROOK_END =
            BitBoard.offset(BLACK_Q_CASTLE_END, 0,  1);

    private static final long WHITE_K_CASTLE_ROOK_MOVE =
            WHITE_K_ROOK_START ^ WHITE_K_CASTLE_ROOK_END;
    private static final long WHITE_Q_CASTLE_ROOK_MOVE =
            WHITE_Q_ROOK_START ^ WHITE_Q_CASTLE_ROOK_END;
    private static final long BLACK_K_CASTLE_ROOK_MOVE =
            BLACK_K_ROOK_START ^ BLACK_K_CASTLE_ROOK_END;
    private static final long BLACK_Q_CASTLE_ROOK_MOVE =
            BLACK_Q_ROOK_START ^ BLACK_Q_CASTLE_ROOK_END;

    private static final long WHITE_K_CASTLE_ALL_MOVES =
            WHITE_K_CASTLE_MOVE ^ WHITE_K_CASTLE_ROOK_MOVE;
    private static final long WHITE_Q_CASTLE_ALL_MOVES =
            WHITE_Q_CASTLE_MOVE ^ WHITE_Q_CASTLE_ROOK_MOVE;
    private static final long BLACK_K_CASTLE_ALL_MOVES =
            BLACK_K_CASTLE_MOVE ^ BLACK_K_CASTLE_ROOK_MOVE;
    private static final long BLACK_Q_CASTLE_ALL_MOVES =
            BLACK_Q_CASTLE_MOVE ^ BLACK_Q_CASTLE_ROOK_MOVE;


    //--------------------------------------------------------------------
    private static final byte EP_NONE       = -1;
    private static final byte EP_WHITE_DEST = 5;
    private static final byte EP_BLACK_DEST = 2;

    public  static final String FILES = "abcdefgh";


    //--------------------------------------------------------------------
    private static final int PAWNS   = Figure.PAWN  .ordinal();
    private static final int KNIGHTS = Figure.KNIGHT.ordinal();
    private static final int BISHOPS = Figure.BISHOP.ordinal();
    private static final int ROOKS   = Figure.ROOK  .ordinal();
    private static final int QUEENS  = Figure.QUEEN .ordinal();
    private static final int KING    = Figure.KING  .ordinal();

    private static final int[]   NON_KINGS_BY_PROB        =
            {PAWNS, ROOKS, BISHOPS, KNIGHTS, QUEENS};

    private static final Piece[] NON_KINGS_BY_PROB_PIECES =
            {Piece.WHITE_PAWN  , Piece.BLACK_PAWN,
             Piece.WHITE_ROOK  , Piece.BLACK_ROOK,
             Piece.WHITE_BISHOP, Piece.BLACK_BISHOP,
             Piece.WHITE_KNIGHT, Piece.BLACK_KNIGHT,
             Piece.WHITE_QUEEN , Piece.BLACK_QUEEN};


    //--------------------------------------------------------------------
    private long[] wPieces;
    private long[] bPieces;

    private long   whiteBB;
    private long   blackBB;

    private byte enPassant; // available to take for nextToAct
    private byte   prevEnPassants;

    private byte   castles;
    private byte   prevCastles;
    private long   castlePath;
    private long   prevCastlePath;
    private byte   reversibleMoves;
    private byte   prevReversibleMoves;

    private Colour nextToAct;


    //--------------------------------------------------------------------
    public State(String fen)
    {
        loadFen(fen);
    }
    public State()
    {
        loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR " +
                    "w KQkq - 0 1");
    }

    private State(long[] copyWPieces,
                  long[] copyBPieces,
                  byte   copyEnPassants,
                  byte   copyCastles,
                  byte   copyReversibleMoves,
                  Colour copyNextToAct,
                  long   copyWhiteBB,
                  long   copyBlackBB,
                  byte   copyPrevCastles,
                  byte   copyPrevReversibleMoves,
                  byte   copyPrevEnPassants,
                  long   copyCastlePath,
                  long   copyPrevCastlePath
            )
    {
        wPieces = copyWPieces;
        bPieces = copyBPieces;

        castles         = copyCastles;
        nextToAct       = copyNextToAct;
        castlePath      = copyCastlePath;
        enPassant = copyEnPassants;
        reversibleMoves = copyReversibleMoves;

        whiteBB = copyWhiteBB;
        blackBB = copyBlackBB;

        prevCastles         = copyPrevCastles;
        prevCastlePath      = copyPrevCastlePath;
        prevEnPassants      = copyPrevEnPassants;
        prevReversibleMoves = copyPrevReversibleMoves;
    }


    //--------------------------------------------------------------------
    public void loadFen(String fen)
    {
        wPieces    = new long[ Figure.VALUES.length ];
        bPieces    = new long[ Figure.VALUES.length ];
        castles    = 0;
        enPassant = EP_NONE;
        whiteBB    = 0;
        blackBB    = 0;

        String[] parts = fen.split(" ");
        String[] ranks = parts[0].split("/");

        for (int rank = 7; rank >= 0; rank--) {
            int file = 0;
            for (char fenPiece : ranks[7 - rank].toCharArray()) {
                if (Character.isDigit(fenPiece)) {
                    int emptyFiles = Character.digit(fenPiece, 10);
                    file += emptyFiles;
                } else {
                    Piece piece = Piece.valueOf(fenPiece);

                    (piece.isWhite()
                     ? wPieces
                     : bPieces)[ piece.figure().ordinal() ]
                            |=  BitLoc.locationToBitBoard(
                                    rank, file++);
                }
            }
        }

        nextToAct = parts[1].equals("w")
                    ? Colour.WHITE : Colour.BLACK;

        if (parts.length >= 3 && (! parts[2].equals("-"))) {
            for (char castle : parts[2].toCharArray()) {
                switch (castle) {
                    case 'K': castles |= WHITE_K_CASTLE; break;
                    case 'Q': castles |= WHITE_Q_CASTLE; break;
                    case 'k': castles |= BLACK_K_CASTLE; break;
                    case 'q': castles |= BLACK_Q_CASTLE; break;
                }
            }
        }

        if (parts.length >= 4 && (! parts[3].equals("-"))) {
            enPassant = (byte) FILES.indexOf(parts[3].charAt(0));
        }

        if (parts.length >= 5 && (! parts[4].equals("-"))) {
            reversibleMoves = Byte.parseByte(parts[4]);
        }

        for (Figure f : Figure.VALUES) {
            whiteBB |= wPieces[ f.ordinal() ];
            blackBB |= bPieces[ f.ordinal() ];
        }

        castlePath          = 0;
        prevCastlePath      = 0;
        prevCastles         = castles;
        prevReversibleMoves = reversibleMoves;
    }


    //--------------------------------------------------------------------
    public int[] legalMoves(/*TranspositionTable transTable*/)
    {
        int[] legalMoves = new int[ Move.MAX_PER_PLY ];
        int   nMoves     = legalMoves(legalMoves/*, transTable*/);

        if (nMoves == -1) return null;
        return Arrays.copyOf(legalMoves, nMoves);
    }
//    public int legalMoves(int[] moves)
//    {
//        return legalMoves(moves, new NullTransTable());
//    }
    public int legalMoves(
            int[] moves/*, TranspositionTable transTable*/)
    {
        int pseudoMoves[] = new int[ Move.MAX_PER_PLY ];
        int nPseudoMoves  = moves(pseudoMoves);
        if (nPseudoMoves == -1) return -1;

        int nextMoveIndex = 0;
        for (int i = 0; i < nPseudoMoves; i++)
        {
            int pseudoMove = pseudoMoves[ i ];
            int undoable   = Move.apply(pseudoMove, this);

            if (! isInCheck(nextToAct.invert())/* &&
                    ! transTable.contains(longHashCode())*/) {
                moves[ nextMoveIndex++ ] = undoable;
            }

            Move.unApply(undoable, this);
        }
        return nextMoveIndex;
    }

    /**
     * generate all pseudo-legal moves from this position
     *  i.e. moves at the end of which you might have your king in check
     *
     * @param moves generate moves into
     * @return number of moves generated, or -1 if mate is possible
     */
    public int moves(int[] moves)
    {
        long occupied    = whiteBB | blackBB;
        long notOccupied = ~occupied;

        long proponent, opponent, oppKing, pieces[];
        if (nextToAct == Colour.WHITE) {
            proponent = whiteBB;
            opponent  = blackBB;
            oppKing   = bPieces[ KING ];
            pieces    = wPieces;
        } else {
            proponent = blackBB;
            opponent  = whiteBB;
            oppKing   = wPieces[ KING ];
            pieces    = bPieces;
        }

        oppKing  |= castlePath;
        opponent |= castlePath;

        long notProponent = ~proponent;
        long notOpponent  = ~opponent;

        int offset = 0;
        for (Figure f : Figure.VALUES)
        {
            long bb = pieces[ f.ordinal() ];
            while (bb != 0)
            {
                long pieceBoard  = BitBoard.lowestOneBit(bb);
                long pseudoMoves = Piece.valueOf(nextToAct, f).moves(
                        pieceBoard, occupied, notOccupied,
                        proponent, notProponent, opponent);
                if ((oppKing & pseudoMoves) != 0) {
                    // can mate opponent's king, so either the opponent
                    //  left his king in check (i.e. made a pseudo-legal
                    //  move which has to be undone), or the game
                    //  is over with nextToAct being the winner
                    return -1;
                }

                offset = addMoves(
                        f, pieceBoard, moves, offset,
                        pseudoMoves, opponent, notOpponent);

                // reset LS1B
                bb &= bb - 1;
            }
        }

//        return offset;
        return addCastles(moves, offset,
                proponent, opponent);
    }

    private int addMoves(
            Figure figure,
            long   fromBB,
            int[]  moves,
            int    offset,
            long   movesBB,
            long   opponent,
            long   notOpponent)
    {
        int from = BitLoc.bitBoardToLocation(fromBB);

        int nextOffset;
        nextOffset = addMobility(
                figure, from, moves, offset, movesBB & notOpponent);
        nextOffset = addCaptures(
                figure, from, moves, nextOffset, movesBB & opponent);

        if (figure == Figure.PAWN) {
            if (canPromote(from)) {
                nextOffset = addPromotions(
                        moves, nextOffset - offset, nextOffset);
            }
            else if (canEnPassant(from))
            {
                nextOffset = addEnPassant(
                        from, moves, nextOffset);
            }
        }
        return nextOffset;
    }

    private int addMobility(
            Figure figure,
            int    from,
            int[]  moves,
            int    offset,
            long   moveBB)
    {
        while (moveBB != 0)
        {
            long moveBoard = BitBoard.lowestOneBit(moveBB);
            moves[ offset++ ] = Move.mobility(
                    figure, from, BitLoc.bitBoardToLocation(moveBoard));

            moveBB &= moveBB - 1;
        }
        return offset;
    }

    private int addCaptures(
            Figure figure,
            int    from,
            int[]  moves,
            int    offset,
            long   moveBB)
    {
        while (moveBB != 0)
        {
            long moveBoard = BitBoard.lowestOneBit(moveBB);
            moves[ offset++ ] = Move.capture(
                    figure, from, BitLoc.bitBoardToLocation(moveBoard));

//            int move = moves[ offset - 1 ];
//            if (! check(move)) {
//                check(move);
//            }

            moveBB &= moveBB - 1;
        }
        return offset;
    }


    //--------------------------------------------------------------------
    private int addCastles(
            int[] moves, int offset,
            long proponent, long opponent)
    {
        int  kingStart, qKingEnd, kKingEnd;
        long kingCastle , kingCorridor;
        long queenCastle, queenCorridor;
        if (nextToAct == Colour.WHITE) {
            if ((castles & WHITE_CASTLE) == 0) return offset;
            kingCastle    = WHITE_K_CASTLE;
            queenCastle   = WHITE_Q_CASTLE;
            kingCorridor  = WHITE_K_CASTLE_CORRIDOR;
            queenCorridor = WHITE_Q_CASTLE_CORRIDOR;
            kingStart     = WHITE_KING_START_INDEX;
            kKingEnd      = WHITE_K_CASTLE_END_INDEX;
            qKingEnd      = WHITE_Q_CASTLE_END_INDEX;
        } else {
            if ((castles & BLACK_CASTLE) == 0) return offset;
            kingCastle    = BLACK_K_CASTLE;
            queenCastle   = BLACK_Q_CASTLE;
            kingCorridor  = BLACK_K_CASTLE_CORRIDOR;
            queenCorridor = BLACK_Q_CASTLE_CORRIDOR;
            kingStart     = BLACK_KING_START_INDEX;
            kKingEnd      = BLACK_K_CASTLE_END_INDEX;
            qKingEnd      = BLACK_Q_CASTLE_END_INDEX;
        }

        int  newOffset = offset;
        long allPieces = proponent | opponent;

        if ((castles & kingCastle) != 0 &&
                (allPieces & kingCorridor) == 0) {
            moves[ newOffset++ ] = Move.castle(
                    kingStart, kKingEnd, CastleType.KING_SIDE);
        }
        if ((castles & queenCastle) != 0 &&
                (allPieces & queenCorridor) == 0) {
            moves[ newOffset++ ] = Move.castle(
                    kingStart, qKingEnd, CastleType.QUEEN_SIDE);
        }

        return newOffset;
    }

    public void castle(CastleType type)
    {
        prevCastles    = castles;
        prevCastlePath = castlePath;
        toggleCastle(type);

        nextToAct           = nextToAct.invert();
        prevReversibleMoves = reversibleMoves;
//        reversibleMoves     = 0;
        reversibleMoves++;
        prevEnPassants      = enPassant;
        enPassant = EP_NONE;
    }

    public void unCastle(CastleType type)
    {
        nextToAct = nextToAct.invert();
        toggleCastle(type);

        castles         = prevCastles;
        enPassant = prevEnPassants;
        reversibleMoves = prevReversibleMoves;
        castlePath      = prevCastlePath;
    }

    private void toggleCastle(CastleType type)
    {
        if (nextToAct == Colour.WHITE) {
            if (type == CastleType.KING_SIDE) {
                wPieces[ KING  ] ^= WHITE_K_CASTLE_MOVE;
                wPieces[ ROOKS ] ^= WHITE_K_CASTLE_ROOK_MOVE;
                whiteBB          ^= WHITE_K_CASTLE_ALL_MOVES;
                castlePath        = WHITE_K_CASTLE_PATH;
            } else {
                wPieces[ KING  ] ^= WHITE_Q_CASTLE_MOVE;
                wPieces[ ROOKS ] ^= WHITE_Q_CASTLE_ROOK_MOVE;
                whiteBB          ^= WHITE_Q_CASTLE_ALL_MOVES;
                castlePath        = WHITE_Q_CASTLE_PATH;
            }
            clearCastlingRights(WHITE_CASTLE);
        } else {
            if (type == CastleType.KING_SIDE) {
                bPieces[ KING  ] ^= BLACK_K_CASTLE_MOVE;
                bPieces[ ROOKS ] ^= BLACK_K_CASTLE_ROOK_MOVE;
                blackBB          ^= BLACK_K_CASTLE_ALL_MOVES;
                castlePath        = BLACK_K_CASTLE_PATH;
            } else {
                bPieces[ KING  ] ^= BLACK_Q_CASTLE_MOVE;
                bPieces[ ROOKS ] ^= BLACK_Q_CASTLE_ROOK_MOVE;
                blackBB          ^= BLACK_Q_CASTLE_ALL_MOVES;
                castlePath        = BLACK_Q_CASTLE_PATH;
            }
            clearCastlingRights(BLACK_CASTLE);
        }
    }

    private void updateCastlingRightsFrom(
            int figure, int from)
    {
        if (figure == KING) {
            if (castles == 0) return;
            clearCastlingRights(
                    (nextToAct == Colour.WHITE)
                    ? WHITE_CASTLE : BLACK_CASTLE);
        } else if (figure == ROOKS) {
            if (castles == 0) return;
            if (nextToAct == Colour.WHITE) {
                if (from == 0) {
                    clearCastlingRights(WHITE_Q_CASTLE);
                } else if (from == 7) {
                    clearCastlingRights(WHITE_K_CASTLE);
                }
            } else {
                if (from == 56) {
                    clearCastlingRights(BLACK_Q_CASTLE);
                } else if (from == 63) {
                    clearCastlingRights(BLACK_K_CASTLE);
                }
            }
        }
    }
    private void updateCastlingRightsTo(
            int figure, long to)
    {
        if (figure != ROOKS || castles == 0) return;
        if (nextToAct == Colour.WHITE) {
            if ((to & BLACK_K_ROOK_START) != 0) {
                clearCastlingRights(BLACK_K_CASTLE);
            } else if ((to & BLACK_Q_ROOK_START) != 0) {
                clearCastlingRights(BLACK_Q_CASTLE);
            }
        } else {
            if ((to & WHITE_K_ROOK_START) != 0) {
                clearCastlingRights(WHITE_K_CASTLE);
            } else if ((to & WHITE_Q_ROOK_START) != 0) {
                clearCastlingRights(WHITE_Q_CASTLE);
            }
        }
    }

    private void clearCastlingRights(byte rights)
    {
        castles &= ~rights;
    }


    //--------------------------------------------------------------------
    private boolean canPromote(int from)
    {
        int fromRank = Location.rankIndex(from);
        if (nextToAct == Colour.WHITE) {
            if (fromRank == 6) {
                return true;
            }
        } else if (fromRank == 1) {
            return true;
        }
        return false;
    }
    private int addPromotions(
            int[] moves, int nMoves, int addAt)
    {
        if (nMoves == 0) return addAt;

        int addFrom   = addAt - nMoves;
        int nextAddAt = addFrom;
        for (int f = KNIGHTS; f < KING; f++) {
            for (int i = 0; i < nMoves; i++) {
                moves[ nextAddAt++ ] =
                        Move.setPromotion(
                                moves[addFrom + i], f);

//                int move = moves[ nextAddAt - 1 ];
//                if (! check(move)) {
//                    check(move);
//                }
            }
        }
        return nextAddAt;
    }


    //--------------------------------------------------------------------
    public void pushPromote(int from, int to, int promotion)
    {
        pushPromoteBB(nextToAct, from, to, promotion);

        nextToAct           = nextToAct.invert();
        prevReversibleMoves = reversibleMoves;
        reversibleMoves     = 0;
        prevEnPassants      = enPassant;
        enPassant = EP_NONE;
        prevCastlePath      = castlePath;
        castlePath          = 0;
        prevCastles         = castles;
    }
    private void pushPromoteBB(
            Colour colour, int from, int to, int promotion) {
        long fromBB = BitLoc.locationToBitBoard(from);
        long toBB   = BitLoc.locationToBitBoard(to);
        if (colour == Colour.WHITE) {
            wPieces[ PAWNS     ] ^= fromBB;
            wPieces[ promotion ] ^= toBB;
            whiteBB              ^= fromBB ^ toBB;
        } else {
            bPieces[ PAWNS     ] ^= fromBB;
            bPieces[ promotion ] ^= toBB;
            blackBB              ^= fromBB ^ toBB;
        }
    }

    public void capturePromote(
            int from, int to, int promotion, int captured)
    {
        long toBB = BitLoc.locationToBitBoard(to);
        capturePromote(from, toBB, promotion, captured);
    }
    public int capturePromote(int from, int to, int promotion)
    {
        long toBB = BitLoc.locationToBitBoard(to);
        int  captured = figureAt(toBB, nextToAct.invert());

        capturePromote(from, toBB, promotion, captured);
        return captured;
    }
    private void capturePromote(
            int from, long toBB, int promotion, int captured)
    {
        capturePromoteBB(nextToAct, from, toBB, promotion, captured);

        prevCastles         = castles;
        updateCastlingRightsTo(captured, toBB);
        prevCastlePath      = castlePath;
        castlePath          = 0;

        nextToAct           = nextToAct.invert();
        prevReversibleMoves = reversibleMoves;
        reversibleMoves     = 0;
        prevEnPassants      = enPassant;
        enPassant = EP_NONE;
    }
    private void capturePromoteBB(Colour colour,
            int from, long toBB, int promotion, int captured)
    {
        long fromBB = BitLoc.locationToBitBoard(from);

        if (colour == Colour.WHITE) {
            wPieces[ PAWNS     ] ^= fromBB;
            wPieces[ promotion ] ^= toBB;
            whiteBB              ^= fromBB ^ toBB;
            bPieces[ captured  ] ^= toBB;
            blackBB              ^= toBB;
        } else {
            bPieces[ PAWNS     ] ^= fromBB;
            bPieces[ promotion ] ^= toBB;
            blackBB              ^= fromBB ^ toBB;
            wPieces[ captured  ] ^= toBB;
            whiteBB              ^= toBB;
        }
    }


    //--------------------------------------------------------------------
    public void unPushPromote(int from, int to, int promotion)
    {
        nextToAct       = nextToAct.invert();
        enPassant = prevEnPassants;
        reversibleMoves = prevReversibleMoves;
        castles         = prevCastles;
        castlePath      = prevCastlePath;

        pushPromoteBB(nextToAct, from, to, promotion);
    }

    public void unCapturePromote(
            int from, int to, int promotion, int captured)
    {
        nextToAct       = nextToAct.invert();
        castles         = prevCastles;
        castlePath      = prevCastlePath;
        enPassant = prevEnPassants;
        reversibleMoves = prevReversibleMoves;

        long toBB = BitLoc.locationToBitBoard(to);
        capturePromoteBB(nextToAct, from, toBB, promotion, captured);
    }


    //--------------------------------------------------------------------
    public void mobalize(
            int figure,
            int fromSquareIndex,
            int toSquareIndex)
    {
        prevCastles = castles;
        updateCastlingRightsFrom(
                figure, fromSquareIndex);

        mobalizeBB(nextToAct, figure,
                   BitLoc.locationToBitBoard(fromSquareIndex),
                   BitLoc.locationToBitBoard(toSquareIndex));

        prevEnPassants      = enPassant;
        enPassant = EP_NONE;
        prevCastlePath      = castlePath;
        castlePath          = 0;
        prevReversibleMoves = reversibleMoves;

        if (figure == PAWNS) {
            reversibleMoves = 0;

            updateEnPassantRights(
                    fromSquareIndex,
                    toSquareIndex);
        } else {
            reversibleMoves++;
        }
    }
    private void mobalizeBB(
            Colour colour,
            int    figure,
            long   from,
            long   to)
    {
        long fromTo = from ^ to;

        if (colour == Colour.WHITE) {
            wPieces[ figure ] ^= fromTo;
            whiteBB           ^= fromTo;
        } else {
            bPieces[ figure ] ^= fromTo;
            blackBB           ^= fromTo;
        }

        nextToAct = nextToAct.invert();
    }


    //--------------------------------------------------------------------
    public void unMobalize(
            int figure,
            int fromSquareIndex,
            int toSquareIndex)
    {
        mobalizeBB(nextToAct.invert(), figure,
                   BitLoc.locationToBitBoard(fromSquareIndex),
                   BitLoc.locationToBitBoard(toSquareIndex));

        castles         = prevCastles;
        castlePath      = prevCastlePath;
        enPassant = prevEnPassants;
        reversibleMoves = prevReversibleMoves;
    }


    //--------------------------------------------------------------------
    public int capture(
            int attacker,
            int fromSquareIndex,
            int toSquareIndex)
    {
        long toBB     = BitLoc.locationToBitBoard(toSquareIndex);
        int  captured = figureAt(toBB, nextToAct.invert());
        if (captured == -1 || captured >= Figure.VALUES.length) {
            figureAt(toBB, nextToAct.invert());
        }
        capture(attacker, fromSquareIndex, toBB, captured);
        return captured;
    }
    public void capture(
            int attacker,
            int fromSquareIndex,
            int toSquareIndex,
            int captured)
    {
        long toBB = BitLoc.locationToBitBoard(toSquareIndex);
        capture(attacker, fromSquareIndex, toBB, captured);
    }
    private void capture(
            int  attacker,
            int  fromSquareIndex,
            long toBB,
            int  captured)
    {
        prevCastles = castles;
        updateCastlingRightsFrom(
                attacker, fromSquareIndex);
        updateCastlingRightsTo(
                captured, toBB);

        capture(attacker, captured,
                BitLoc.locationToBitBoard(fromSquareIndex), toBB);

        prevReversibleMoves = reversibleMoves;
        prevEnPassants      = enPassant;
        enPassant = EP_NONE;
        reversibleMoves     = 0;
        prevCastlePath      = castlePath;
        castlePath          = 0;
    }
    private void capture(
            int  attacker,
            int  captured,
            long from,
            long to)
    {
        long fromTo = from ^ to;

        if (nextToAct == Colour.WHITE) {
            wPieces[attacker] ^= fromTo;
            bPieces[captured] ^= to;

            whiteBB ^= fromTo;
            blackBB ^= to;
        } else {
            bPieces[attacker] ^= fromTo;
            wPieces[captured] ^= to;

            blackBB ^= fromTo;
            whiteBB ^= to;
        }

        nextToAct = nextToAct.invert();
    }


    //--------------------------------------------------------------------
    public void unCapture(
            int attacker,
            int captured,
            int fromSquareIndex,
            int toSquareIndex)
    {
        long from   = BitLoc.locationToBitBoard(fromSquareIndex);
        long to     = BitLoc.locationToBitBoard(  toSquareIndex);
        long fromTo = from ^ to;

        if (nextToAct == Colour.WHITE) {
            // black is the attacker
            bPieces[ attacker ] ^= fromTo;
            wPieces[ captured ] ^= to;

            blackBB ^= fromTo;
            whiteBB ^= to;
        } else {
            wPieces[ attacker ] ^= fromTo;
            bPieces[ captured ] ^= to;

            whiteBB ^= fromTo;
            blackBB ^= to;
        }

        nextToAct       = nextToAct.invert();
        castles         = prevCastles;
        enPassant = prevEnPassants;
        reversibleMoves = prevReversibleMoves;
    }


    //--------------------------------------------------------------------
    private boolean canEnPassant(int from)
    {
        if (enPassant == EP_NONE) return false;

        int rank = Location.rankIndex(from);
        if (nextToAct == Colour.WHITE) {
             if (rank != 4) return false;
        }
        else if (rank != 3) return false;

        int file = Location.fileIndex(from);
        return enPassant == (file - 1) ||
               enPassant == (file + 1);
    }

    private int addEnPassant(
            int from, int moves[], int nextOffset)
    {
        if (nextToAct == Colour.BLACK) {
            moves[nextOffset] = Move.enPassant(from,
                    Location.squareIndex(EP_BLACK_DEST, enPassant));
        } else {
            moves[nextOffset] = Move.enPassant(from,
                    Location.squareIndex(EP_WHITE_DEST, enPassant));
        }
        return nextOffset + 1;
    }

    public void enPassantCapture(
            int from, int to, int captured)
    {
        enPassantSwaps(from, to, captured);

        nextToAct           = nextToAct.invert();
        prevEnPassants      = enPassant;
        enPassant = EP_NONE;
        prevReversibleMoves = reversibleMoves;
        reversibleMoves     = 0;
        prevCastlePath      = castlePath;
        castlePath          = 0;
    }
    public void unEnPassantCapture(
            int from, int to, int captured)
    {
        nextToAct       = nextToAct.invert();
        enPassant = prevEnPassants;
        reversibleMoves = prevReversibleMoves;
        castlePath      = prevCastlePath;

        enPassantSwaps(from, to, captured);
    }
    private void enPassantSwaps(
            int from, int to, int captured)
    {
        long fromBB = BitLoc.locationToBitBoard(from);
        long toBB   = BitLoc.locationToBitBoard(to);
        long capBB  = BitLoc.locationToBitBoard(captured);

        long fromToBB = fromBB ^ toBB;
        if (nextToAct == Colour.WHITE) {
            wPieces[ PAWNS ] ^= fromToBB;
            whiteBB          ^= fromToBB;
            bPieces[ PAWNS ] ^= capBB;
            blackBB          ^= capBB;
        } else {
            bPieces[ PAWNS ] ^= fromToBB;
            blackBB          ^= fromToBB;
            wPieces[ PAWNS ] ^= capBB;
            whiteBB          ^= capBB;
        }
    }

    // requires that a Figure.PAWN is moving
    public void updateEnPassantRights(int from, int to)
    {
        if (Math.abs(Location.rankIndex(from) -
                     Location.rankIndex(to  )) > 1) {
            enPassant = (byte) Location.fileIndex(from);
        }
    }


    //--------------------------------------------------------------------
    public int tallyNonKings(int atMost) {
        int tally = 0, count;
        for (int figure : NON_KINGS_BY_PROB) {
            count = Long.bitCount(wPieces[ figure ]);
            tally = MaterialTally.tally(
                        tally, Colour.WHITE, figure, count);
            if ((atMost -= count) <= 0) return -1;

            count = Long.bitCount(bPieces[ figure ]);
            tally = MaterialTally.tally(
                        tally, Colour.BLACK, figure, count);
            if ((atMost -= count) <= 0) return -1;
        }
        return tally;
    }
    public boolean tallyNonKings(MaterialTally tally, int atMost) {
        for (int figure : NON_KINGS_BY_PROB) {
            atMost -= tally.tally(Colour.WHITE, figure,
                                  Long.bitCount(wPieces[ figure ]));
            if (atMost <= 0) return false;

            atMost -= tally.tally(Colour.BLACK, figure,
                                  Long.bitCount(wPieces[ figure ]));
            if (atMost <= 0) return false;
        }
        return true;
    }

    public boolean atMostPieces(int n) {
        int remain = n - 2; // kings
        if (remain <= 0) return false;

        for (int figure : NON_KINGS_BY_PROB) {
            remain -= Long.bitCount(wPieces[ figure ]);
            if (remain <= 0) return true;

            remain -= Long.bitCount(bPieces[ figure ]);
            if (remain <= 0) return true;
        }

        return true;
    }

    public List<Piece> material() {
        List<Piece> material = new ArrayList<Piece>(4);
        material(material, wPieces);
        material(material, bPieces);
        return material;
    }
    private void material(List<Piece> addTo, long[] pieces) {
        for (Figure f : Figure.VALUES) {
            long bb = pieces[ f.ordinal() ];
            while (bb != 0) {
                addTo.add(Piece.valueOf(nextToAct, f));
                bb &= bb - 1;
            }
        }
    }


    //--------------------------------------------------------------------
    public boolean isInCheck(Colour colour)
    {
        long occupied    = whiteBB | blackBB;
        long notOccupied = ~occupied;

        long attacker, attacked, targetKing, attackingPieces[];
        if (colour == Colour.BLACK) {
            attacker        = whiteBB;
            attacked        = blackBB;
            targetKing      = bPieces[ KING ];
            attackingPieces = wPieces;
        } else {
            attacker        = blackBB;
            attacked        = whiteBB;
            targetKing      = wPieces[ KING ];
            attackingPieces = bPieces;
        }

        if (colour != nextToAct && castlePath != 0) {
            targetKing  = castlePath;
            attacked   |= castlePath;
        }

        long notAttacker = ~attacker;

        Colour attackColour = colour.invert();
        for (Figure f : Figure.VALUES)
        {
            Piece p  = Piece.valueOf(attackColour, f);
            long  bb = attackingPieces[ f.ordinal() ];
            while (bb != 0)
            {
                long pieceBoard  = BitBoard.lowestOneBit(bb);
                long pseudoMoves = p.moves(
                        pieceBoard, occupied, notOccupied,
                        attacker, notAttacker, attacked);
                if ((targetKing & pseudoMoves) != 0) return true;
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
    public boolean isDrawnBy50MovesRule() {
        return reversibleMoves > 100;
    }

    public Outcome knownOutcome() {
        if (isDrawnBy50MovesRule()) return Outcome.DRAW;

        int moves[] = legalMoves();
        if (moves == null) {
            if (isInCheck( nextToAct() )) {
                return Outcome.loses( nextToAct() );
            }
        } else if (moves.length == 0) {
            if (isInCheck( nextToAct() )) {
                return Outcome.loses( nextToAct() );
            } else {
                return Outcome.DRAW;
            }
        }

        return null;
    }


    //--------------------------------------------------------------------
    private Piece pieceAt(int rankIndex, int fileIndex)
    {
        long loc = BitLoc.locationToBitBoard(rankIndex, fileIndex);
        for (Figure f : Figure.VALUES) {
            if ((wPieces[ f.ordinal() ] & loc) != 0)
                return Piece.valueOf(Colour.WHITE, f);

            if ((bPieces[ f.ordinal() ] & loc) != 0)
                return Piece.valueOf(Colour.BLACK, f);
        }
        return null;
    }

    private int figureAt(long location, Colour ofColour)
    {
        long[] pieces = (ofColour == Colour.WHITE)
                        ? wPieces : bPieces;

        for (int f = 0; f < Figure.VALUES.length; f++)
        {
            long occupied = pieces[ f ];
            if ((occupied & location) != 0) return f;
        }
        return -1;
    }


    //--------------------------------------------------------------------
    public State prototype()
    {
        return new State(wPieces.clone(),
                         bPieces.clone(),
                enPassant,
                         castles,
                         reversibleMoves,
                         nextToAct,
                         whiteBB,
                         blackBB,
                         prevCastles,
                         prevReversibleMoves,
                         prevEnPassants,
                         castlePath,
                         prevCastlePath
               );
    }


    //--------------------------------------------------------------------
    public boolean checkPieces()
    {
        if (!(whiteBB == calcPieces(Colour.WHITE) &&
              blackBB == calcPieces(Colour.BLACK))) {
//            System.out.println("checkPieces: colourBB failed");
            return false;
        }

        if (Long.bitCount(wPieces[ KING ]) != 1 ||
            Long.bitCount(bPieces[ KING ]) != 1) {
//            System.out.println("checkPieces: not exactly one king");
            return false;
        }

        if (enPassant != EP_NONE) {
            if (nextToAct == Colour.WHITE) {
                if (pieceAt(3, enPassant) == Piece.WHITE_PAWN &&
                        pieceAt(2, enPassant) == null &&
                        pieceAt(1, enPassant) == null) {
                    return false;
                }
            } else {
                if (pieceAt(4, enPassant) == Piece.WHITE_PAWN &&
                        pieceAt(5, enPassant) == null &&
                        pieceAt(6, enPassant) == null) {
                    return false;
                }
            }
        }
        
        if ((wPieces[PAWNS] & BitBoard.RANK_1) != 0 ||
                (bPieces[PAWNS] & BitBoard.RANK_8) != 0) {
            return false;
        }

        if ((castles & WHITE_CASTLE) != 0) {
            if ((wPieces[ KING ] & WHITE_KING_START) == 0) {
//                System.out.println("white can't castle after king moved");
                return false;
            }
            if ((castles & WHITE_K_CASTLE) != 0 &&
                    (wPieces[ ROOKS ] & WHITE_K_ROOK_START) == 0) {
//                System.out.println("white k castle impossible");
                return false;
            }
            if ((castles & WHITE_Q_CASTLE) != 0 &&
                    (wPieces[ ROOKS ] & WHITE_Q_ROOK_START) == 0) {
//                System.out.println("white q castle impossible");
                return false;
            }
        }
        if ((castles & BLACK_CASTLE) != 0) {
            if ((bPieces[ KING ] & BLACK_KING_START) == 0) {
//                System.out.println("black can't castle after king moved");
                return false;
            }
            if ((castles & BLACK_K_CASTLE) != 0 &&
                    (bPieces[ ROOKS ] & BLACK_K_ROOK_START) == 0) {
//                System.out.println("black k castle impossible");
                return false;
            }
            if ((castles & BLACK_Q_CASTLE) != 0 &&
                    (bPieces[ ROOKS ] & BLACK_Q_ROOK_START) == 0) {
//                System.out.println("black q castle impossible");
                return false;
            }
        }

        return true;
    }

    private long calcPieces(Colour c)
    {
        long[] pieces = (c == Colour.WHITE)
                        ? wPieces : bPieces;

        long bb = 0;
        for (Figure f : Figure.VALUES)
        {
            bb |= pieces[ f.ordinal() ];
        }
        return bb;
    }

//    public boolean check(int move)
//    {
//        State myClone = prototype();
//        move = Move.apply(move, myClone);
//        boolean afterDo   = myClone.checkPieces();
//        if (! afterDo) {
//            System.out.println("before: " + toString());
//            System.out.println("move: " + Move.toString(move));
//            System.out.println("after: " + myClone.toString());
//        }
//
//        Move.unApply(move, myClone);
//        boolean afterUndo = myClone.checkPieces();
//        return afterDo && afterUndo;
//    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        StringBuffer str = new StringBuffer();

        str.append("Next to Act: ").append(nextToAct);
        str.append("\nReversible Moves: ").append(reversibleMoves);

        if (castles != 0) {
            str.append("\nCastles Available: ");

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

        if (enPassant != 0) {
            str.append("\nEn Passants: ");
            if (enPassant != EP_NONE) {
                str.append(enPassant);
            }
        }

        for (int rank = 7; rank >= 0; rank--)
        {
            str.append("\n");
            for (int file = 0; file < 8; file++)
            {
                Piece p = pieceAt(rank, file);
                str.append((p == null) ? "." : p);
            }
        }

        return str.toString();
    }


    //--------------------------------------------------------------------
    public String toFen()
    {
        StringBuilder str = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < 8; file++) {
                Piece p = pieceAt(rank, file);
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

        if (castles == 0) {
            str.append("-");
        } else {
            if ((castles & WHITE_K_CASTLE) != 0) str.append("K");
            if ((castles & WHITE_Q_CASTLE) != 0) str.append("Q");

            if ((castles & BLACK_K_CASTLE) != 0) str.append("k");
            if ((castles & BLACK_Q_CASTLE) != 0) str.append("q");
        }
        str.append(" ");

		// En passant square
        if (enPassant == EP_NONE) {
            str.append("-");
        } else {
            str.append(FILES.charAt(enPassant));
//            str.append(" ");
            if (nextToAct == Colour.WHITE) {
                str.append(EP_WHITE_DEST + 1);
            } else {
                str.append(EP_BLACK_DEST + 1);
            }
        }
        str.append(" ");

        str.append(reversibleMoves);
        str.append(" ");
        str.append("n"); // full moves since start of game

		return str.toString();
    }


    //--------------------------------------------------------------------
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;
        return castlePath == state.castlePath &&
                castles == state.castles &&
                enPassant == state.enPassant &&
                reversibleMoves == state.reversibleMoves &&
                Arrays.equals(bPieces, state.bPieces) &&
                nextToAct == state.nextToAct &&
                Arrays.equals(wPieces, state.wPieces);
    }

    @Override public int hashCode() {
        int result = Arrays.hashCode(wPieces);
        result = 31 * result + Arrays.hashCode(bPieces);
        result = 31 * result + (int) enPassant;
        result = 31 * result + (int) castles;
        result = 31 * result + (int) (castlePath ^ (castlePath >>> 32));
        result = 31 * result + (int) reversibleMoves;
        result = 31 * result + nextToAct.hashCode();
        return result;
    }

    public long longHashCode() {
        return nextToActPostprocess(
                Zobrist.toggleReversibleMoves(
                        staticHashCode(), reversibleMoves));
    }
    public long staticHashCode() {
        return nextToActPostprocess(
                zobristPiecesEnPassantCastles());
    }
    private long zobristPiecesEnPassantCastles() {
        long zobrist = 0;

        zobrist = addZobristPieces(zobrist, Colour.WHITE);
        zobrist = addZobristPieces(zobrist, Colour.BLACK);

        if (enPassant != EP_NONE) {
            zobrist = Zobrist.toggleEnPassant(zobrist, enPassant);
        }

        return addZobristCastles(zobrist);
    }
    private long nextToActPostprocess(long zobrist) {
        return (nextToAct == Colour.WHITE && zobrist < 0 ||
                nextToAct == Colour.BLACK && zobrist >= 0)
               ? -zobrist : zobrist;
    }
    private long addZobristPieces(
            long zobrist, Colour side) {
        long[] pieces =
                (side == Colour.WHITE)
                ? wPieces : bPieces;

        for (Figure f : Figure.VALUES) {
            Piece piece = Piece.valueOf(side, f);

            long bb = pieces[ f.ordinal() ];
            while (bb != 0) {
                long pieceBoard = BitBoard.lowestOneBit(bb);
                int  location   = BitLoc.bitBoardToLocation(pieceBoard);

                zobrist = Zobrist.togglePiece(zobrist, piece, location);

                // reset LS1B
                bb &= bb - 1;
            }
        }
        return zobrist;
    }
    private long addZobristCastles(long zobrist) {
        if ((castles & WHITE_K_CASTLE) != 0) {
            zobrist = Zobrist.toggleCastle(
                    zobrist, Colour.WHITE, CastleType.KING_SIDE);
        }
        if ((castles & WHITE_Q_CASTLE) != 0) {
            zobrist = Zobrist.toggleCastle(
                    zobrist, Colour.WHITE, CastleType.QUEEN_SIDE);
        }
        if ((castles & BLACK_K_CASTLE) != 0) {
            zobrist = Zobrist.toggleCastle(
                    zobrist, Colour.BLACK, CastleType.KING_SIDE);
        }
        if ((castles & BLACK_Q_CASTLE) != 0) {
            zobrist = Zobrist.toggleCastle(
                    zobrist, Colour.BLACK, CastleType.QUEEN_SIDE);
        }
        return zobrist;
    }

    public static boolean hashOfWhiteToAct(long longOrStaticHash) {
        return longOrStaticHash >= 0;
    }
}
