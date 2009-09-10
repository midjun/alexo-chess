package v2.state;

import v2.data.BitBoard;
import v2.data.BitLoc;
import v2.data.Location;
import v2.move.SlidingPieces;
import v2.piece.Colour;
import v2.piece.Figure;
import v2.piece.Piece;

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


    //--------------------------------------------------------------------
    private static final int PAWNS   = Figure.PAWN  .ordinal();
    private static final int KNIGHTS = Figure.KNIGHT.ordinal();
    private static final int BISHOPS = Figure.BISHOP.ordinal();
    private static final int ROOKS   = Figure.ROOK  .ordinal();
    private static final int QUEENS  = Figure.QUEEN .ordinal();
    private static final int KING    = Figure.KING  .ordinal();


    //--------------------------------------------------------------------
    private long[] wPieces;
    private long[] bPieces;

    private long   whiteBB;
    private long   blackBB;

    private byte   enPassants; // available to take for nextToAct
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
        wPieces = new long[ Figure.VALUES.length ];
        bPieces = new long[ Figure.VALUES.length ];
    }
    public State()
    {
        wPieces = new long[ Figure.VALUES.length ];
        bPieces = new long[ Figure.VALUES.length ];

        for (int p = 0; p < 8; p++) {
            wPieces[ PAWNS ] |=  BitLoc.locationToBitBoard(1, p);
            bPieces[ PAWNS ] |=  BitLoc.locationToBitBoard(6, p);
        }

        wPieces[ ROOKS ] = BitLoc.locationToBitBoard(0, 0) |
                           BitLoc.locationToBitBoard(0, 7);
        bPieces[ ROOKS ] = BitLoc.locationToBitBoard(7, 0) |
                           BitLoc.locationToBitBoard(7, 7);

        wPieces[ KNIGHTS ] = BitLoc.locationToBitBoard(0, 1) |
                             BitLoc.locationToBitBoard(0, 6);
        bPieces[ KNIGHTS ] = BitLoc.locationToBitBoard(7, 1) |
                             BitLoc.locationToBitBoard(7, 6);

        wPieces[ BISHOPS ] = BitLoc.locationToBitBoard(0, 2) |
                             BitLoc.locationToBitBoard(0, 5);
        bPieces[ BISHOPS ] = BitLoc.locationToBitBoard(7, 2) |
                             BitLoc.locationToBitBoard(7, 5);

        wPieces[ QUEENS ] = BitLoc.locationToBitBoard(0, 3);
        bPieces[ QUEENS ] = BitLoc.locationToBitBoard(7, 3);

        wPieces[ KING ] = BitLoc.locationToBitBoard(0, 4);
        bPieces[ KING ] = BitLoc.locationToBitBoard(7, 4);

        enPassants = EP_NONE;
        castles    = WHITE_CASTLE | BLACK_CASTLE;

        reversibleMoves = 0;
        nextToAct       = Colour.WHITE;

        for (Figure f : Figure.VALUES) {
            whiteBB |= wPieces[ f.ordinal() ];
            blackBB |= bPieces[ f.ordinal() ];
        }

        castlePath          = 0;
        prevCastlePath      = 0;
        prevCastles         = castles;
        prevReversibleMoves = reversibleMoves;
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
        enPassants      = copyEnPassants;
        reversibleMoves = copyReversibleMoves;

        whiteBB = copyWhiteBB;
        blackBB = copyBlackBB;

        prevCastles         = copyPrevCastles;
        prevCastlePath      = copyPrevCastlePath;
        prevEnPassants      = copyPrevEnPassants;
        prevReversibleMoves = copyPrevReversibleMoves;
    }


    //--------------------------------------------------------------------
    public int legalMoves(int[] moves)
    {
        int pseudoMoves[] = new int[ 256 ];
        int nPseudoMoves  = moves(pseudoMoves);
        if (nPseudoMoves == -1) return -1;

        int nextMoveIndex = 0;
        for (int i = 0; i < nPseudoMoves; i++)
        {
            int pseudoMove = pseudoMoves[ i ];
            int undoable   = Move.apply(pseudoMove, this);

            if (! isInCheck(nextToAct.invert())) {
                moves[ nextMoveIndex++ ] = pseudoMove;
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
        oppKing |= castlePath;

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

            int move = moves[ offset - 1 ];
//            if (! check(move)) {
//                check(move);
//            }

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

            int move = moves[ offset - 1 ];
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
        long kingCastle , kingCorridor;
        long queenCastle, queenCorridor;
        if (nextToAct == Colour.WHITE) {
            if ((castles & WHITE_CASTLE) == 0) return offset;
            kingCastle    = WHITE_K_CASTLE;
            queenCastle   = WHITE_Q_CASTLE;
            kingCorridor  = WHITE_K_CASTLE_CORRIDOR;
            queenCorridor = WHITE_Q_CASTLE_CORRIDOR;
        } else {
            if ((castles & BLACK_CASTLE) == 0) return offset;
            kingCastle    = BLACK_K_CASTLE;
            queenCastle   = BLACK_Q_CASTLE;
            kingCorridor  = BLACK_K_CASTLE_CORRIDOR;
            queenCorridor = BLACK_Q_CASTLE_CORRIDOR;
        }

        int  newOffset = offset;
        long allPieces = proponent | opponent;

        if ((castles & kingCastle) != 0 &&
                (allPieces & kingCorridor) == 0) {
            int moveAddend = Move.castle(CastleType.KING_SIDE);
//            if (! check(moveAddend)) {
//                check(moveAddend);
//            }
            moves[ newOffset++ ] = Move.castle(CastleType.KING_SIDE);
        }
        if ((castles & queenCastle) != 0 &&
                (allPieces & queenCorridor) == 0) {
            int moveAddend = Move.castle(CastleType.QUEEN_SIDE);
//            if (! check(moveAddend)) {
//                check(moveAddend);
//            }
            moves[ newOffset++ ] = Move.castle(CastleType.QUEEN_SIDE);
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
        reversibleMoves     = 0;
        prevEnPassants      = enPassants;
        enPassants          = EP_NONE;
    }

    public void unCastle(CastleType type)
    {
        nextToAct = nextToAct.invert();
        toggleCastle(type);

        castles         = prevCastles;
        enPassants      = prevEnPassants;
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

                int move = moves[ nextAddAt - 1 ];
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
        prevEnPassants      = enPassants;
        enPassants          = EP_NONE;
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
        // todo need to add history of moves, and go back
        //  one by one until i can track down the source of this wierd issue:
        //  it seems to come up whenever a pawn captures a knight to promote
        //  to a knight

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
        prevEnPassants      = enPassants;
        enPassants          = EP_NONE;
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
        enPassants      = prevEnPassants;
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
        enPassants      = prevEnPassants;
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

        prevEnPassants      = enPassants;
        enPassants          = EP_NONE;
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
        enPassants      = prevEnPassants;
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
        prevEnPassants      = enPassants;
        enPassants          = EP_NONE;
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
        enPassants      = prevEnPassants;
        reversibleMoves = prevReversibleMoves;
    }


    //--------------------------------------------------------------------
    private boolean canEnPassant(int from)
    {
        if (enPassants == EP_NONE) return false;

        int rank = Location.rankIndex(from);
        if (nextToAct == Colour.WHITE) {
             if (rank != 4) return false;
        }
        else if (rank != 3) return false;

        int file = Location.fileIndex(from);
        return enPassants == (file - 1) ||
               enPassants == (file + 1);
    }

    private int addEnPassant(
            int from, int moves[], int nextOffset)
    {
        if (nextToAct == Colour.BLACK) {
            moves[nextOffset] = Move.enPassant(from,
                    Location.squareIndex(EP_BLACK_DEST, enPassants));
        } else {
            moves[nextOffset] = Move.enPassant(from,
                    Location.squareIndex(EP_WHITE_DEST, enPassants));
        }
        return nextOffset + 1;
    }

    public void enPassantCapture(
            int from, int to, int captured)
    {
        enPassantSwaps(from, to, captured);

        nextToAct           = nextToAct.invert();
        prevEnPassants      = enPassants;
        enPassants          = EP_NONE;
        prevReversibleMoves = reversibleMoves;
        reversibleMoves     = 0;
        prevCastlePath      = castlePath;
        castlePath          = 0;
    }
    public void unEnPassantCapture(
            int from, int to, int captured)
    {
        nextToAct       = nextToAct.invert();
        enPassants      = prevEnPassants;
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
            enPassants = (byte) Location.fileIndex(from);
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
            targetKing = castlePath;
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
    // see http://chessprogramming.wikispaces.com/Draw+evaluation
    // can later be substituted with a tablebase
    public Status knownStatus()
    {
        if (reversibleMoves > 100) return Status.DRAW;

        // at least one major piece (i.e. rook or queen)
        if (wPieces[ ROOKS  ] != 0 ||
            bPieces[ ROOKS  ] != 0 ||
            wPieces[ QUEENS ] != 0 ||
            bPieces[ QUEENS ] != 0) return Status.IN_PROGRESS;

        boolean whiteBishops, blackBishops;
        boolean whiteKnights, blackKnights;

        boolean whitePawns = (wPieces[ PAWNS ] != 0);
        boolean blackPawns = (bPieces[ PAWNS ] != 0);
        if (whitePawns && blackPawns) {
            return Status.IN_PROGRESS;
        } else {
            whiteBishops = (wPieces[ BISHOPS ] != 0);
            blackBishops = (bPieces[ BISHOPS ] != 0);

            whiteKnights = (wPieces[ KNIGHTS ] != 0);
            blackKnights = (bPieces[ KNIGHTS ] != 0);

            if (whitePawns || blackPawns) {
                // at least one side has at least a minor pawn
                if (whiteBishops || blackBishops ||
                    whiteKnights || blackKnights) {
                    return Status.IN_PROGRESS;
                } else {
                    if (whitePawns) {
                        int nWhitePawns =
                                Long.bitCount(wPieces[ PAWNS ]);
                        return (nWhitePawns == 1)
                               ? Status.DRAW : Status.IN_PROGRESS;
                    } else {
                        int nBlackPawns =
                                Long.bitCount(bPieces[ PAWNS ]);
                        return (nBlackPawns == 1)
                               ? Status.DRAW : Status.IN_PROGRESS;
                    }
                }
            }
        }
        // only knights and bishops present, no pawns, queens, or rooks

        if (whiteBishops && blackBishops) {
            if (whiteKnights || blackKnights){
                return Status.IN_PROGRESS;
            }

            // both sides have a king and a bishop,
            //   the bishops being the same color
            int nWhiteBishops = Long.bitCount(wPieces[ BISHOPS ]);
            if (nWhiteBishops > 1) return Status.IN_PROGRESS;

            int nBlackBishops = Long.bitCount(wPieces[ BISHOPS ]);
            if (nBlackBishops > 1) return Status.IN_PROGRESS;

            return (BitBoard.isDark(wPieces[ BISHOPS ]) ==
                    BitBoard.isDark(bPieces[ BISHOPS ]))
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
                    Long.bitCount(wPieces[ KNIGHTS ]);

            //one side has two or more knights against the bare king
            return (nWhiteKnights <= 2)
                    ? Status.DRAW : Status.IN_PROGRESS;
        } else if (blackKnights) {
            int nBlackKnights =
                    Long.bitCount(bPieces[ KNIGHTS ]);
            return (nBlackKnights <= 2)
                    ? Status.DRAW : Status.IN_PROGRESS;
        }
        return Status.DRAW;
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
                         enPassants,
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
            System.out.println("checkPieces: colourBB failed");
            return false;
        }

        if (Long.bitCount(wPieces[ KING ]) != 1 ||
            Long.bitCount(bPieces[ KING ]) != 1) {
            System.out.println("checkPieces: not exactly one king");
            return false;
        }

        if ((castles & WHITE_CASTLE) != 0) {
            if ((wPieces[ KING ] & WHITE_KING_START) == 0) {
                System.out.println("white can't castle after king moved");
                return false;
            }
            if ((castles & WHITE_K_CASTLE) != 0 &&
                    (wPieces[ ROOKS ] & WHITE_K_ROOK_START) == 0) {
                System.out.println("white k castle impossible");
                return false;
            }
            if ((castles & WHITE_Q_CASTLE) != 0 &&
                    (wPieces[ ROOKS ] & WHITE_Q_ROOK_START) == 0) {
                System.out.println("white q castle impossible");
                return false;
            }
        }
        if ((castles & BLACK_CASTLE) != 0) {
            if ((bPieces[ KING ] & BLACK_KING_START) == 0) {
                System.out.println("black can't castle after king moved");
                return false;
            }
            if ((castles & BLACK_K_CASTLE) != 0 &&
                    (bPieces[ ROOKS ] & BLACK_K_ROOK_START) == 0) {
                System.out.println("black k castle impossible");
                return false;
            }
            if ((castles & BLACK_Q_CASTLE) != 0 &&
                    (bPieces[ ROOKS ] & BLACK_Q_ROOK_START) == 0) {
                System.out.println("black q castle impossible");
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

    public boolean check(int move)
    {
        State myClone = prototype();
        move = Move.apply(move, myClone);
        boolean afterDo   = myClone.checkPieces();
        if (! afterDo) {
            System.out.println("before: " + toString());
            System.out.println("move: " + Move.toString(move));
            System.out.println("after: " + myClone.toString());
        }

        Move.unApply(move, myClone);
        boolean afterUndo = myClone.checkPieces();
        return afterDo && afterUndo;
    }


    //--------------------------------------------------------------------
    public String toString() {
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

        if (enPassants != 0) {
            str.append("\nEn Passants: ");
            if (enPassants != EP_NONE) {
                str.append(enPassants);
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
        str.append("-"); // [a .. h] + " " + {3, 6}
        str.append(" ");

        str.append(reversibleMoves);
        str.append(" ");
        str.append("n"); // full moves since start of game

		return str.toString();
    }
}
