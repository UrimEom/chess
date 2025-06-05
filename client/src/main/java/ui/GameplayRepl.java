package ui;

import chess.*;

import java.io.PrintStream;
import java.util.Scanner;

import static java.lang.System.out;

public class GameplayRepl {
    private final ChessGame game;
    private final ChessGame.TeamColor color;
    ServerFacade server;
    private final Scanner scanner;

    public GameplayRepl(ServerFacade server, ChessGame game, ChessGame.TeamColor color) {
        this.game = game;
        this.color = color;
        this.server = server;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while(true) {
            String input = scanner.nextLine().trim();
            String[] inputs = input.split(" ");

            if(inputs.length == 0 || inputs[0].isEmpty()) { continue; }

            String command = inputs[0].toLowerCase();

            switch (command) {
                case "help" -> printHelp();
                case "redraw" -> drawBoard();
                case "leave" -> {
                    return;
                }
                case "make" -> {
                    if(inputs.length >= 2 && inputs[1].equalsIgnoreCase("move")) {
                        handleMakeMove();
                    }else {
                        out.println("USE: make move");
                    }
                }
                case "resign" -> {
                    handleResign();
                    return;
                }
                default -> {
                    out.println("Invalid command: please try again");
                    printHelp();
                }
            }

        }
    }

    private void printHelp() {
        out.println("help -> to show possible commands");
        out.println("redraw -> to redraw the chess board");
        out.println("leave -> to leave the game");
        out.println("make <FROM> <TO> <PROMOTION_PIECE> -> to make a move on the board");
        out.println("resign -> to forfeit the game");
        out.println("highlight -> to highlight legal moves");
    }

    private void handleMakeMove() {
        out.println("Please enter the move you want (ex: b7 b6): ");
        String answer = scanner.next().trim();
        String[] elements = answer.split(" ");
        if(elements.length != 2 && elements[0].matches("[a-h][1-8]") && elements[1].matches("[a-h][1-8]")) {
            out.println("Please enter two squares (ex: b7 b6)");
            return;
        }

        try {
            ChessPosition from = new ChessPosition(elements[0].charAt(1) - '0', elements[0].charAt(0) - ('a'-1));
            ChessPosition to = new ChessPosition(elements[1].charAt(1) - '0', elements[1].charAt(0) - ('a'-1));

            ChessMove move = new ChessMove(from, to, null); //*add promotion piece later

            try {
                game.makeMove(move);
                drawBoard();
            }catch (InvalidMoveException ex) {
                out.println("Invalid move: " + ex.getMessage());
            }
        }catch(IllegalArgumentException ex) {
            out.println("Please provide proper square like b7 or b6.");
        }

    }


    public void drawBoard() {
        try {
            ChessBoard board = game.getBoard();

            //color background
            out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            //draw headers
            out.print("   ");
            drawBoardLabel(out);

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
            drawBoardLabel(out);

            out.print("   ");
            out.print(EscapeSequences.RESET_TEXT_COLOR);
            out.print(EscapeSequences.RESET_BG_COLOR);
            out.println();

        }catch(Exception ex) {
            System.out.println("Display the board failed. Please try again.");
        }
    }

    private void drawBoardLabel(PrintStream out) {
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
                drawBoardSquare(out, board.getPiece(pos), row, col - 'a' + 1);
            }
        }else {
            for(char col = 'a'; col <= 'h'; col++) {
                ChessPosition pos = new ChessPosition(row, col - 'a' + 1);
                drawBoardSquare(out, board.getPiece(pos), row, col - 'a' + 1);
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

    private void drawBoardSquare(PrintStream out, ChessPiece piece, int row, int col) {
        //color square
        if((row + col) % 2 == 1) {
            out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        }else {
            out.print(EscapeSequences.SET_BG_COLOR_BLACK);
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
