package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebsocketHandler {
    private static final Map<Session, Integer> GAME_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Session>> IN_GAME_SESSION = new ConcurrentHashMap<>();
    private static final Map<Session, AuthData> AUTHSESSIONS = new ConcurrentHashMap<>();
    private static final Gson GSON = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket is connect: " + session);
    }

   @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        JsonObject obj = JsonParser.parseString(message).getAsJsonObject();
        String rawType;
        if(obj.has("command_type")) {
            rawType = obj.get("command_type").getAsString();
        }else if(obj.has("commandType")) {
            rawType = obj.get("commandType").getAsString();
        }else {
            sendError(session, new ErrorMessage("Error: Invalid command type"));
            return;
        }

        String type = rawType.toUpperCase();
        switch (type) {
            case "CONNECT" -> {
                ConnectCommand command = GSON.fromJson(message, ConnectCommand.class);
                handleConnect(session, command);
            }
            case "MAKE_MOVE" -> {
                MakeMoveCommand command = GSON.fromJson(message, MakeMoveCommand.class);
                handleMakeMove(session, command);
            }
            case "RESIGN" -> {
                ResignCommand command = GSON.fromJson(message, ResignCommand.class);
                handleResign(session, command);
            }
            case "LEAVE" -> {
                LeaveCommand command = GSON.fromJson(message, LeaveCommand.class);
                handleLeave(session, command);
            }
            default -> send(session, new ErrorMessage("Unknown command type."));
        }
    }

    private AuthData validateAuth(Session session, String authToken) {
        try {
            return Server.USER_SERVICE.getAuth(authToken);
        } catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unauthorized"));
            return null;
        }
    }

    private GameData validateGameData(Session session, String authToken, int gameID) {
        try {
            GameData gameData = Server.GAME_SERVICE.getGameData(authToken, gameID);
            if(gameData == null) {
                sendError(session, new ErrorMessage("Error: Invalid game"));
            }
            return gameData;
        } catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Invalid game"));
            return null;
        }
    }

    private GameData validateInitializedGame(Session session, String authToken, GameData gameData) {
        if(gameData.game() == null) {
            GameData fresh = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), new ChessGame());
            try {
                Server.GAME_SERVICE.updateGame(authToken, fresh);
            }catch (DataAccessException ex) {
                //ignore
            }
            return fresh;
        }
        return gameData;
    }

    private void handleConnect(Session session, ConnectCommand command) {
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();

        AuthData auth = validateAuth(session, authToken);
        if(auth == null) { return; }

        AUTHSESSIONS.put(session, auth);
        GAME_SESSIONS.put(session, gameID);

        GameData gameData = validateGameData(session, authToken, gameID);
        if(gameData == null) { return; }

        Set<Session> existing = IN_GAME_SESSION.computeIfAbsent(gameID, k -> new HashSet<>());
        //        Set<Session> existing = IN_GAME_SESSION.getOrDefault(gameID, Collections.emptySet());
        String username = auth.username();
        String player = username.equals(gameData.whiteUsername())
                ? "white player" : username.equals(gameData.blackUsername())
                ? "black player" :"observer";
        NotificationMessage notification = new NotificationMessage(username + " connected as " + player);
        for(Session other : existing) {
            if(other.isOpen() && !other.equals(session)) {
                send(other, notification);
            }
        }

//        if(!IN_GAME_SESSION.containsKey(gameID)) {
//            IN_GAME_SESSION.put(gameID, new HashSet<>());
//        }
//        IN_GAME_SESSION.get(gameID).add(session);
        existing.add(session);

        send(session, new LoadMessage(gameData));
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        AuthData auth = validateAuth(session, authToken);
        if(auth == null) { return; }

        GameData gameData = validateGameData(session, authToken, gameID);
        if(gameData == null) { return; }

        gameData = validateInitializedGame(session, authToken, gameData);

        ChessGame game = gameData.game();

        ChessGame.TeamColor userColor = getTeamColor(auth.username(), gameData);
        if (userColor == null) {
            sendError(session, new ErrorMessage("Error: Observing game"));
            return;
        }

        if (game.getGameOver()) {
            sendError(session, new ErrorMessage("Error: Game is over"));
            return;
        }

        if (!game.getTeamTurn().equals(userColor)) {
            sendError(session, new ErrorMessage("Error: Not your turn"));
            return;
        }

        try {
            game.makeMove(command.getMove());
        } catch (InvalidMoveException ex) {
            sendError(session, new ErrorMessage("Error: Invalid move"));
            return;
        }

        try {
            Server.GAME_SERVICE.updateGame(authToken, gameData);
        } catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unable to persist game"));
            return;
        }

        LoadMessage load = new LoadMessage(gameData);

        String user = auth.username();

        ChessMove move = command.getMove();
        ChessPosition from = move.getStartPosition();
        ChessPosition to = move.getEndPosition();
        String moveDescription = from.toString() + " -> " + to.toString();
        NotificationMessage notification = new NotificationMessage(String.format("%s moved: %s", user, moveDescription));
        Set<Session> existing = IN_GAME_SESSION.get(gameID);
//        Set<Session> existing = IN_GAME_SESSION.getOrDefault(gameID, Collections.emptySet());
        for(Session s : existing) {
            if(!s.equals(session) && s.isOpen()) {
                send(s, notification);
            }
            send(s, load);
        }

        ChessGame.TeamColor enemy = game.getTeamTurn();
        String enemyName = (enemy == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername(): gameData.blackUsername();
        if(game.isInCheckmate(enemy)) {
            NotificationMessage inCheckmate = new NotificationMessage(String.format("%s is in checkmate! Game over.", enemyName));
            for(Session s : existing) {
                send(s, inCheckmate);
            }
        }else if(game.isInCheck(enemy)) {
            NotificationMessage inCheck = new NotificationMessage(String.format("%s is in check!", enemyName));
            for(Session s : existing) {
                send(s, inCheck);
            }
        }
    }

    private void handleResign(Session session, ResignCommand command) {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        AuthData auth = validateAuth(session, authToken);
        if(auth == null) { return; }

        GameData gameData = validateGameData(session, authToken, gameID);
        if(gameData == null) { return; }

        gameData = validateInitializedGame(session, authToken, gameData);

        ChessGame.TeamColor userColor = getTeamColor(auth.username(), gameData);
        if(userColor == null) {
            sendError(session, new ErrorMessage("Error: Observing game"));
            return;
        }

        if(gameData.game().getGameOver()) {
            sendError(session, new ErrorMessage("Error: Game is over"));
            return;
        }

        gameData.game().setGameOver(true);

        try {
            Server.GAME_SERVICE.updateGame(authToken, gameData);
        }catch (DataAccessException ex) { //ignore
        }

        String user = auth.username() + " resigned";
        Set<Session> existing = IN_GAME_SESSION.getOrDefault(gameID, Collections.emptySet());
        for(Session s: existing) {
            if(s.equals(session)) {
                send(s, new NotificationMessage(user)); //notify the user who resigned
            }else { //everyone else
                send(s, new NotificationMessage(user));
            }
        }
    }

    private void handleLeave(Session session, LeaveCommand command) {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        AuthData auth = validateAuth(session, authToken);
        if(auth == null) { return; }

        GameData gameData = validateGameData(session, authToken, gameID);
        if(gameData == null) { return; }

        IN_GAME_SESSION.getOrDefault(gameID, Set.of()).remove(session);
        String username = auth.username();
        String player = username.equals(gameData.whiteUsername())
                ? "White player" : username.equals(gameData.blackUsername())
                ? "Black player" :"Observer";
        sendToGame(gameID, new NotificationMessage(String.format("%s-%s: left the game.", player, username)));
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Integer gameID = GAME_SESSIONS.remove(session);
        if(gameID != null) {
            IN_GAME_SESSION.getOrDefault(gameID, Set.of()).remove(session);
        }
        AUTHSESSIONS.remove(session);
        System.out.println("WebSocket close: " + reason);
    }

    private void sendToGame(int gameID, ServerMessage message) {
        for(Session s: IN_GAME_SESSION.getOrDefault(gameID, Set.of())) {
            if(s.isOpen()) {
                send(s, message);
            }
        }
    }

    private void send(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(GSON.toJson(message));
        }catch (IOException e) {
            System.err.println("Send error: " + e.getMessage());
        }
    }

    private void sendError(Session session, ErrorMessage errorMessage) {
        try {
            session.getRemote().sendString(GSON.toJson(errorMessage));
        }catch (Exception e) {
             //ignore
        }
    }

    private ChessGame.TeamColor getTeamColor(String username, GameData game) {
        if(username.equals(game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }else if(username.equals(game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }else {
            return null;
        }
    }
}
