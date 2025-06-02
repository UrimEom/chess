import chess.*;
import ui.GameplayRepl;
import ui.PreloginRepl;
import ui.ServerFacade;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        //draw board
        ChessGame game = new ChessGame();
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        GameplayRepl repl = new GameplayRepl(game, color);
        repl.drawBoard();

        String serverUrl = "http://localhost:8080";

        if(args.length == 1) {
            serverUrl = args[0];
        }
        ServerFacade server = new ServerFacade(serverUrl);

        PreloginRepl preloginUI = new PreloginRepl(server);
        preloginUI.run();
        System.out.println("END");

    }
}