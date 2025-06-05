package ui;

public class ServerMessage {

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private ServerMessageType serverMessageType;
    private Object game;
    private String errorMessage;
    private String notificationMessage;

    public ServerMessage() { }

    public ServerMessage(Object gameObj) {
        this.serverMessageType = ServerMessageType.LOAD_GAME;
        this.game = gameObj;
    }

    public ServerMessage(String error, boolean isError) {
        this.serverMessageType = ServerMessageType.ERROR;
        this.errorMessage = error;
    }

    public ServerMessage(String notification) {
        this.serverMessageType = ServerMessageType.NOTIFICATION;
        this.notificationMessage = notification;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public void setServerMessageType(ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    public Object getGame() {
        return game;
    }

    public void setGame(Object game) {
        this.game = game;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}
