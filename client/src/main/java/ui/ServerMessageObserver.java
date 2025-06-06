package ui;

import websocket.messages.ServerMessage;

//not sure to use this

public interface ServerMessageObserver {
    void notify(ServerMessage message);
}
