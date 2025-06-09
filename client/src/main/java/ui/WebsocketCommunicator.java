package ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebsocketCommunicator {
    private final ServerMessageObserver observer;
    private Session session;
    private final Gson gson = new Gson();

    public WebsocketCommunicator(ServerMessageObserver observer) {
        this.observer = observer;
    }

    public void connect(String wsUrl) throws DeploymentException, IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(wsUrl));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        JsonObject jsonObj = JsonParser.parseString(message).getAsJsonObject();
        String typeStr = jsonObj.get("serverMessageType").getAsString();
        ServerMessage.ServerMessageType type = ServerMessage.ServerMessageType.valueOf(typeStr);

        switch (type) {
            case LOAD_GAME -> {
                LoadMessage loadMessage = gson.fromJson(message, LoadMessage.class);
                observer.notify(loadMessage);
            }
            case ERROR -> {
                ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                observer.notify(errorMessage);
            }
            case NOTIFICATION -> {
                NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
                observer.notify(notificationMessage);
            }
            default -> new ErrorMessage("Unknown server message type: " + type);
            }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    public void sendCommand(UserGameCommand command) {
        if(session == null || !session.isOpen()) {
            throw new IllegalStateException("Websocket was not opened");
        }
        String json = gson.toJson(command);
        session.getAsyncRemote().sendText(json);
    }
}
