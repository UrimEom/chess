package ui;

import chess.*;
import model.GameData;
import websocket.messages.LoadMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static java.lang.System.out;

public class GameplayRepl implements ServerMessageObserver {
    private final ServerFacade server;
    private final GameData gameData;
    private ChessGame game = new ChessGame();
    private final ChessGame.TeamColor color;
    private final Scanner scanner = new Scanner(System.in);

    private final Set<ChessPosition> highlighted = new HashSet<>();
    private ChessPosition selectedPos = null;

    private boolean isObserver;

    //Constructor for player
    public GameplayRepl(ServerFacade server, GameData gameData, ChessGame game, ChessGame.TeamColor color) {
        this.server = server;
        this.gameData = gameData;
        this.game = game;
        this.color = color;
        this.isObserver = false;

        server.setGameID(gameData.gameID());
        server.setObserver(this);
        server.connectWS();

    }

    //Constructor for observer
    public GameplayRepl(ServerFacade server, GameData gameData, ChessGame game) {
       this.server = server;
       this.gameData = gameData;
       this.game = game;
       this.color = null;
       this.isObserver = true;

       server.setGameID(gameData.gameID());
       server.setObserver(this);
       server.connectWS();
    }

    public void run() {
        System.out.println(" ");
        while(true) {
            System.out.print("\n[GAME PLAY] >>> \n");
            String input = scanner.nextLine().trim();
            String[] inputs = input.split(" ");

            if(inputs.length == 0 || inputs[0].isEmpty()) { continue; }

            String command = inputs[0].toLowerCase();

            switch (command) {
                case "help" -> printHelp();
                case "redraw" -> {
                    selectedPos = null;
                    highlighted.clear();
                    drawBoard();
                }
                case "leave" -> {
                    server.leaveGame(gameData.gameID());
                    return;
                }
                case "move" -> {
                    if(inputs.length >= 3 && !isObserver) {
                        handleMakeMove(inputs);
                    }else {
                        out.println("USE: move <FROM> <TO> <PROMOTION_PIECE>");
                    }
                }
                case "resign" -> {
                    if(!isObserver) {
                        handleResign();
                        return;
                    }
                }
                case "highlight" -> {
                    if(inputs.length == 2 && inputs[1].matches("[a-h][1-8]")) {
                        handleHighlight(inputs);
                    }else {
                        out.println("USE: highlight <square> (ex: highlight b6)");
                    }
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
        out.println("move <FROM> <TO> <PROMOTION_PIECE> -> to make a move on the board");
        out.println("resign -> to forfeit the game");
        out.println("highlight <square> -> to highlight legal moves");
    }

    private void handleResign() {
        out.print("Are you sure you want to resign? (YES/NO): ");
        String answer = scanner.next().trim().toUpperCase();
        if(answer.equals("YES")) {
            server.resignGame(gameData.gameID());
        }else {
            out.println("Resignation was cancelled.");
        }
    }
    private void handleMakeMove(String[] elements) {
        ChessPosition from;
        ChessPosition to;
        try {
            from = parseSquare(elements[1]);
            to = parseSquare(elements[2]);
        }catch (IllegalArgumentException ex) {
            out.println("Invalid square. Square should be a1-h8.");
            return;
        }

        if(game.getTeamTurn() != color) {
            out.println("Not your turn. Current turn: " + game.getTeamTurn());
            return;
        }

        ChessPiece piece = game.getBoard().getPiece(from);
        if(piece == null) {
            out.println("No piece at " + elements[1] + " to move.");
            return;
        }

        ChessPiece.PieceType promotionPiece = null;
        boolean isPawn        = piece.getPieceType() == ChessPiece.PieceType.PAWN;
        boolean onLastRank    = (color == ChessGame.TeamColor.WHITE && to.getRow() == 8)
                || (color == ChessGame.TeamColor.BLACK && to.getRow() == 1);
        boolean shouldPromote = isPawn && onLastRank;
        ChessMove move;
        if(elements.length == 4) {
            if (shouldPromote) {
                String option = elements[3].toUpperCase();
                switch (option) {
                    case "QUEEN" -> promotionPiece = ChessPiece.PieceType.QUEEN;
                    case "BISHOP" -> promotionPiece = ChessPiece.PieceType.BISHOP;
                    case "ROOK" -> promotionPiece = ChessPiece.PieceType.ROOK;
                    case "KNIGHT" -> promotionPiece = ChessPiece.PieceType.KNIGHT;
                    default -> out.println("Please choose proper promotion piece.");
                }
            }else {
                out.println("Invalid promotion. Please try again");
                return;
            }
        }
        move = new ChessMove(from, to, promotionPiece);

        try {
            game.makeMove(move);
            server.makeMove(gameData.gameID(), move);
        }catch(Exception e) {
            out.println(e.getMessage());
        }
    }

    private void handleHighlight(String[] elements) {
        String square = elements[1];
        ChessPosition position;

        try {
            position = parseSquare(square);
        }catch (IllegalArgumentException ex) {
            out.println("Invalid square. Please enter square like b2");
            return;
        }

        ChessPiece piece = game.getBoard().getPiece(position);
        if(piece == null) {
            out.println("There is no piece");
            highlighted.clear();
            drawBoard();
            return;
        }

        Collection<ChessMove> moves = game.validMoves(position);
        if(moves == null || moves.isEmpty()) {
            out.println("There is no legal moves for the piece at " + square);
            highlighted.clear();
            drawBoard();
            return;
        }

        selectedPos = position;
        highlighted.clear();
        for(ChessMove m : moves) {
            highlighted.add(m.getEndPosition());
        }

        drawBoard();
        out.println("Highlighted legal moves for " + square);
        highlighted.clear();
        selectedPos = null;
    }

    @Override
    public void notify(ServerMessage message) {
        if(message instanceof NotificationMessage) {
            NotificationMessage notification = (NotificationMessage) message;
            out.println(notification.getMessage());
//            return;
        }

        if(message instanceof LoadMessage) {
            LoadMessage loadMessage = (LoadMessage) message;
            GameData updated = loadMessage.getGame();
            ChessGame model = updated.game();
            if(model != null) {
                this.game = model;
            }
            highlighted.clear();
            out.println("Current turn: " + game.getTeamTurn());
            drawBoard();
            return;
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
            if (this.color == ChessGame.TeamColor.BLACK) {
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

        if(!isBlack) {
            for(char col = 'a'; col <= 'h'; col++) {
                int viewCol = col - 'a' + 1;
                ChessPosition pos = new ChessPosition(row, viewCol);
                drawBoardSquare(out, board.getPiece(pos), row, viewCol);
            }
        }else {
            for(char col = 'h'; col >= 'a'; col--) {
                int viewCol = col - 'a' + 1;
                ChessPosition pos = new ChessPosition(row, viewCol);
                drawBoardSquare(out, board.getPiece(pos), row, viewCol);
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
        ChessPosition position = new ChessPosition(row, col);

        boolean isDark = ((row + col) % 2 == 1);

        //highlight legal moves
        if (position.equals(selectedPos)) {
            out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
        } else if (highlighted.contains(position)) {
            if (isDark) {
                out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
            } else {
                out.print(EscapeSequences.SET_BG_COLOR_GREEN);
            }
        } else if (isDark) {
            out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        } else {
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

    private ChessPosition parseSquare(String str) {
        if (str == null || str.length() != 2) {
            throw new IllegalArgumentException("Square must be 2 characters");
        }
        char alphabet = str.charAt(0);
        char number = str.charAt(1);

        if (alphabet < 'a' || alphabet > 'h' || number < '1' || number > '8') {
            throw new IllegalArgumentException("Square is out of range: " + str);
        }

        int alphabetIndex = alphabet - 'a' + 1;
        int numIndex = number - '1' + 1;

        return new ChessPosition(numIndex, alphabetIndex);
    }

}
