package ao.chess.v2.state;

import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.piece.Figure;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 12-Oct-2009
 * Time: 3:18:38 PM
 */
public class Retrograde
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        long before = System.currentTimeMillis();

        final long wWins[] = {0};
        final long bWins[] = {0};
        final long draws[] = {0};

        new Retrograde().terminals(
                Arrays.asList(
                        Piece.WHITE_KING,
                        Piece.BLACK_KING,
                        Piece.BLACK_PAWN),
                new TerminalVisitor() {
                    @Override
                    public void visit(State state, Outcome outcome) {
                        System.out.println();
                        System.out.println(outcome);
                        System.out.println(state);

                        switch (outcome) {
                            case WHITE_WINS: wWins[0]++; break;
                            case BLACK_WINS: bWins[0]++; break;
                            case DRAW:       draws[0]++; break;
                        }
                    }
                }
        );

        System.out.println("found: " +
                wWins[0] + " white wins, " +
                bWins[0] + " black wins, " +
                draws[0] + " draws");
        System.out.println(
                "took " + (System.currentTimeMillis() - before));
    }


    //--------------------------------------------------------------------
    private final Piece[][] BOARD;


    //--------------------------------------------------------------------
    public Retrograde() {
        BOARD = new Piece[ Location.RANKS ]
                         [ Location.FILES ];
    }


    //--------------------------------------------------------------------
//    public Collection<State> precedents(State of)
//    {
//        return null;
//    }


    //--------------------------------------------------------------------
    public void terminals(
            List<Piece>     pieces,
            TerminalVisitor terminalTraverser)
    {
        place(pieces.get(0),
              pieces.subList(1, pieces.size()),
              terminalTraverser);
    }


    //--------------------------------------------------------------------
    private void place(
            Piece           first,
            List<Piece>     rest,
            TerminalVisitor terminalTraverser)
    {
        for (int rank = 0; rank < Location.RANKS; rank++) {
            if (first.figure() == Figure.PAWN &&
                    (rank == 0 || rank == 7)) continue;
            
            for (int file = 0; file < Location.FILES; file++) {
                if (BOARD[rank][file] != null) continue;

                BOARD[rank][file] = first;

                if (rest.isEmpty()) {
                    check(Colour.WHITE, terminalTraverser);
                    check(Colour.BLACK, terminalTraverser);
                } else {
                    place(rest.get(0),
                          rest.subList(1, rest.size()),
                          terminalTraverser);
                }

                BOARD[rank][file] = null;
            }
        }
    }


    //--------------------------------------------------------------------
    private void check(
            Colour          nextToAct,
            TerminalVisitor terminalVisitor)
    {
        checkTerminal(new State(
                fen(nextToAct, -1)), terminalVisitor);

        int enPassant = enPassant( nextToAct.invert() );
        if (enPassant == -1) return;

        checkTerminal(new State(
                fen(nextToAct, enPassant)), terminalVisitor);
    }

    private void checkTerminal(
            State           state,
            TerminalVisitor terminalVisitor)
    {
        int moves[] = state.legalMoves();
        if (moves == null &&
                state.isInCheck( state.nextToAct() ) &&
                ! state.isInCheck( state.nextToAct().invert() )) {
            terminalVisitor.visit(
                    state, Outcome.loses(state.nextToAct()));
        } else if (moves != null && moves.length == 0) {
            if (state.isInCheck( state.nextToAct() )) {
                terminalVisitor.visit(
                        state, Outcome.loses(state.nextToAct()));
            } else {
                terminalVisitor.visit(
                        state, Outcome.DRAW);
            }
        }
    }


    //--------------------------------------------------------------------
    private int enPassant(Colour by) {
        if (by == Colour.WHITE) {
            for (int file = 0; file < Location.FILES; file++) {
                if (BOARD[3][file] == Piece.WHITE_PAWN &&
                        BOARD[2][file] == null &&
                        BOARD[1][file] == null) {
                    return file;
                }
            }
        } else {
            for (int file = 0; file < Location.FILES; file++) {
                if (BOARD[4][file] == Piece.BLACK_PAWN &&
                        BOARD[5][file] == null &&
                        BOARD[6][file] == null) {
                    return file;
                }
            }
        }
        return -1;
    }


    //--------------------------------------------------------------------
    private String fen(Colour nextToAct, int enPassant)
    {
        StringBuilder str = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < 8; file++) {
                Piece p = BOARD[rank][file];
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
        str.append("-");

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
        str.append("0");

        str.append(" ");

        // full moves since start of game
        str.append("0");

		return str.toString();
    }


    //--------------------------------------------------------------------
    public static interface TerminalVisitor
    {
        public void visit(State state, Outcome outcome);
    }
}
