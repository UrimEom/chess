import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        int port = 8080;
        try {
            Server server = new Server();
            server.run(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}