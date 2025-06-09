package websocket.messages;

import chess.ChessGame;
import model.GameData;

public class LoadMessage extends ServerMessage {
    private GameData game;

    public LoadMessage(GameData game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }

    public void setGame(GameData game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return String.format("Game - White: %s, Black: %s", game.whiteUsername(), game.blackUsername());
    }
}
