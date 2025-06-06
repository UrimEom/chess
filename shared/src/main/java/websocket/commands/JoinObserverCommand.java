package websocket.commands;

public class JoinObserverCommand extends UserGameCommand {
    public JoinObserverCommand(String authToken, Integer gameID) {
        super(CommandType.CONNECT, authToken, gameID);
    }
}
