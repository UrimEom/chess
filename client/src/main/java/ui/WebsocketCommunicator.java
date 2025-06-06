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

public class WebsocketCommunicator {
    private final ServerMessageObserver observer;
    private final UserGameCommand command;
    private Session session;
    private final Gson gson = new Gson();

    public WebsocketCommunicator(ServerMessageObserver observer, UserGameCommand command) throws Exception {
        this.observer = observer;
        this.command = command;
    }

    public void connect(String wsUrl) throws DeploymentException, IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(wsUrl));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        sendCommand(command);
    }

    @OnMessage
    public void onMessage(String message) {
        try {
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
            }

        }catch (Exception ex) {
            ErrorMessage failure = new ErrorMessage(ex.getMessage());
            observer.notify(failure);
        }
    }

    //might need onClose() onError() close()

    public void sendCommand(UserGameCommand cmd) {
        if(session == null || !session.isOpen()) {
            throw new IllegalStateException("Unable to send command");
        }
        String json = gson.toJson(cmd);
        session.getAsyncRemote().sendText(json);
    }
}
