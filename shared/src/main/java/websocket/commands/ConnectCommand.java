package websocket.commands;

import model.AuthData;
import model.GameData;

public class ConnectCommand extends UserGameCommand {
    public ConnectCommand(String authToken, Integer gameID) {
        super(CommandType.CONNECT, authToken, gameID);
    }
}
