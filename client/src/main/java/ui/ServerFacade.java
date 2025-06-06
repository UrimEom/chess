package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import model.UserData;
import websocket.commands.*;

import javax.websocket.Session;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();
    private final HttpCommunicator http;
    private WebsocketCommunicator ws;
    private String authToken;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        http = new HttpCommunicator(this, serverUrl);
    }

    //register
    public AuthData register(String username, String password, String email) {
        return http.register(username, password, email);
    }

    //login
    public AuthData login(String username, String password) {
        return http.login(username, password);
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
    }

    //join player
    public void joinPlayer(int gameID, ChessGame.TeamColor color) {
        UserGameCommand joinPlayerCommand = new JoinPlayerCommand(this.authToken, gameID, color);
        this.ws.sendCommand(joinPlayerCommand);
    }

    //join observer
    public void joinObserver(int gameID) {
        UserGameCommand joinObserverCommand = new JoinObserverCommand(this.authToken, gameID);
        this.ws.sendCommand(joinObserverCommand);
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

//    public void clearServer() {
//        makeRequest("DELETE", "/db", null, Void.class, null);
//    }

    protected void setAuthToken(String authToken) {
        this.authToken = authToken;
        //maybe add exception?
    }
}