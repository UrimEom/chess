package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;

import static java.lang.System.out;

public class GameplayRepl {
    private final ChessGame game;
    private final ChessGame.TeamColor color;

    public GameplayRepl(ChessGame game, ChessGame.TeamColor color) {
        this.game = game;
        this.color = color;
    }

    public void drawBoard() {
        try {
            ChessBoard board = game.getBoard();

            //color background
            out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            //draw headers
            out.print("   ");
            printLabel(out);

            out.print("   ");
            out.print(EscapeSequences.RESET_TEXT_COLOR);
            out.print(EscapeSequences.RESET_BG_COLOR);
            out.println();

            //draw rows
            if (color == ChessGame.TeamColor.BLACK) {
                for (int row = 1; row <= 8; row++) {
                    drawBoardRow(out, board, row, true);
                }
            } else {
                for (int row = 8; row >= 1; row--) {
                    drawBoardRow(out, board, row, false);
                }
            }

            out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);

            //draw footers
            out.print("   ");
            printLabel(out);

            out.print("   ");
            out.print(EscapeSequences.RESET_TEXT_COLOR);
            out.print(EscapeSequences.RESET_BG_COLOR);
            out.println();

        }catch(Exception ex) {
            System.out.println("Display the board failed. Please try again.");
        }
    }

    private void printLabel(PrintStream out) {
        if (color == ChessGame.TeamColor.BLACK) {
            for (char c = 'h'; c >= 'a'; c--) {
                out.printf(" %c ", c);
            }
        } else {
            for (char c = 'a'; c <= 'h'; c++) {
                out.printf(" %c ", c);
            }
        }
    }
    private void drawBoardRow(PrintStream out, ChessBoard board, int row, boolean isBlack) {
        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.print(EscapeSequences.RESET_TEXT_COLOR);

        out.print(" " + row + " ");
        out.print(EscapeSequences.RESET_TEXT_COLOR);

        if(isBlack) {
            for(char col = 'h'; col >= 'a'; col--) {
                ChessPosition pos = new ChessPosition(row, col - 'a' + 1);
                drawSquare(out, board.getPiece(pos), row, col - 'a' + 1);
            }
        }else {
            for(char col = 'a'; col <= 'h'; col++) {
                ChessPosition pos = new ChessPosition(row, col - 'a' + 1);
                drawSquare(out, board.getPiece(pos), row, col - 'a' + 1);
            }
        }
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.print(EscapeSequences.RESET_TEXT_COLOR);

        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.print(EscapeSequences.RESET_TEXT_COLOR);

        out.print(" " + row + " ");

        out.print(EscapeSequences.RESET_TEXT_COLOR);
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.println();
    }

    private void drawSquare(PrintStream out, ChessPiece piece, int row, int col) {
        //color square
        if((row + col) % 2 == 1) {
            out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        }else {
            out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        }

        //color the piece
        if(piece == null) {
            out.print("   ");
        }else {
            if(piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
            }else {
                out.print(EscapeSequences.SET_TEXT_COLOR_RED);
            }

            String symbol = switch (piece.getPieceType()) {
                case KING -> " K ";
                case QUEEN -> " Q ";
                case ROOK -> " R ";
                case BISHOP -> " B ";
                case KNIGHT -> " N ";
                case PAWN -> " P ";
            };
            out.print(symbol);
            out.print(EscapeSequences.RESET_TEXT_COLOR);
        }
    }
}
