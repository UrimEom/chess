package ui;

import chess.ChessMove;
import model.AuthData;
import model.GameData;

import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.util.List;


public class ServerFacade implements ServerMessageObserver {
    private final String serverUrl;
    private final HttpCommunicator http;
    private WebsocketCommunicator ws;
    private ServerMessageObserver observer;
    private String authToken;
    private int gameID;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        http = new HttpCommunicator(this, serverUrl);
        this.observer = null;
    }

    public void setObserver(ServerMessageObserver observer) {
        this.observer = observer;
    }

    //register
    public AuthData register(String username, String password, String email) {
        AuthData auth = http.register(username, password, email);
        setAuthToken(auth.authToken());
        return auth;
    }

    //login
    public AuthData login(String username, String password) {
        AuthData auth = http.login(username, password);
        setAuthToken(auth.authToken());
        return auth;
    }

    //logout
    public void logout(String authToken) {
        http.logout(authToken);
    }

    //create game
    public GameData createGame(String gameName, String authToken) {
        return http.createGame(gameName, authToken);
    }

    //list game
    public List<GameData> listGames(String authToken) {
       return http.listGames(authToken);
    }

    //join game
    public void joinGame(int gameID, String playerColor, String authToken) {
        http.joinGame(gameID, playerColor, authToken);
        setAuthToken(authToken);
        this.gameID = gameID;
    }

    //make move
    public void makeMove(int gameID, ChessMove move) {
        UserGameCommand makeMoveCommand = new MakeMoveCommand(this.authToken, gameID, move);
        this.ws.sendCommand(makeMoveCommand);
    }

    //leave game
    public void leaveGame(int gameID) {
        UserGameCommand leaveCommand = new LeaveCommand(this.authToken, gameID);
        this.ws.sendCommand(leaveCommand);
    }

    //resign game
    public void resignGame(int gameID) {
        UserGameCommand resignCommand = new ResignCommand(this.authToken, gameID);
        this.ws.sendCommand(resignCommand);
    }


    public void clearServer() {
        http.clearServer();
        this.authToken = null;
        this.gameID = 0;
    }

    public String getAuthToken() {
        return this.authToken;
    }

    protected void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void connectWS() {
        try {
            ws = new WebsocketCommunicator(this);
            String base;
            if(serverUrl.startsWith("https://")) {
                base = "wss://" + serverUrl.substring("https://".length());
            }else {
                base = "ws://" + serverUrl.substring("http://".length());
            }
            base = base.replaceAll("/+$", "");
            String wsUrl = base + "/ws";
            ws.connect(wsUrl);

            ws.sendCommand(new ConnectCommand(authToken, this.gameID));
        }catch (Exception e) {
            System.out.println("Failed to make connection: " + e.getMessage());
        }
    }

    @Override
    public void notify(ServerMessage message) {
        if(observer != null) {
            observer.notify(message);
        }else {
            System.out.println("WS message: " + message);
        }
    }
}