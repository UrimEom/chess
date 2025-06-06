package websocket.commands;

import chess.ChessGame;

public class JoinPlayerCommand extends UserGameCommand {
    private final ChessGame.TeamColor color;

    public JoinPlayerCommand(String authToken, Integer gameID, ChessGame.TeamColor color) {
        super(CommandType.CONNECT, authToken, gameID);
        this.color = color;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }
}
