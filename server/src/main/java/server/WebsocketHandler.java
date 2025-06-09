package server;

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
    private static final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Session>> inGameSessions = new ConcurrentHashMap<>();
    private static final Map<Session, AuthData> authSessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

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
                ConnectCommand command = gson.fromJson(message, ConnectCommand.class);
                handleConnect(session, command);
            }
            case "MAKE_MOVE" -> {
                MakeMoveCommand command = gson.fromJson(message, MakeMoveCommand.class);
                handleMakeMove(session, command);
            }
            case "RESIGN" -> {
                ResignCommand command = gson.fromJson(message, ResignCommand.class);
                handleResign(session, command);
            }
            case "LEAVE" -> {
                LeaveCommand command = gson.fromJson(message, LeaveCommand.class);
                handleLeave(session, command);
            }
            default -> send(session, new ErrorMessage("Unknown command type."));
        }
    }

    private void handleConnect(Session session, ConnectCommand command) {
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();
        AuthData auth;
        try {
            auth = Server.userService.getAuth(authToken);
        }catch (DataAccessException e) {
            sendError(session, new ErrorMessage("Error: Unauthorized"));
            return;
        }

        GameData gameData;
        try {
            gameData = Server.gameService.getGameData(authToken, gameID);
        }catch (DataAccessException e) {
            sendError(session, new ErrorMessage("Error: Invalid game"));
            return;
        }

        Set<Session> existing = inGameSessions.getOrDefault(gameID, Collections.emptySet());
        String username = auth.username();
        String player = username.equals(gameData.whiteUsername())
                ? "white player" : username.equals(gameData.blackUsername())
                ? "black player" :"observer";
        NotificationMessage notification = new NotificationMessage(username + "connected as" + player);
        for(Session other : existing) {
            send(other, notification);
        }

        if(!inGameSessions.containsKey(gameID)) {
            inGameSessions.put(gameID, new HashSet<>());
        }
        inGameSessions.get(gameID).add(session);

        send(session, new LoadMessage(gameData));
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) {
        int gameID = command.getGameID();
        //validate and apply the move to game!!!
        sendToGame(gameID, new NotificationMessage("Move made: " + command.getMove()));
    }

    private void handleResign(Session session, ResignCommand command) {
        int gameID = command.getGameID();
        sendToGame(gameID, new NotificationMessage("Player resigned."));
    }

    private void handleLeave(Session session, LeaveCommand command) {
        int gameID = command.getGameID();
        inGameSessions.getOrDefault(gameID, Set.of()).remove(session);
        sendToGame(gameID, new NotificationMessage("Player left the game."));
    }

//    private void handleMakeMove(Session session, MakeMoveCommand command) {
//        int gameID = command.getGameID();
//        String authToken = command.getAuthToken();
//
//        AuthData auth;
//        try {
//            auth = Server.userService.getAuth(authToken);
//        }catch (DataAccessException ex) {
//            sendError(session, new ErrorMessage("Error: Unauthorized"));
//            return;
//        }
//
//        GameData game;
//        try {
//            game = Server.gameService.getGameData(authToken, gameID);
//        }catch (DataAccessException ex) {
//            sendError(session, new ErrorMessage("Error: Invalid game"));
//            return;
//        }
//
//        ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
//        if(userColor == null) {
//            sendError(session, new ErrorMessage("Error: Observing game"));
//            return;
//        }
//
//        if(game.game().getGameOver()) {
//            sendError(session, new ErrorMessage("Error: Game is over"));
//            return;
//        }
//
//        if(!game.game().getTeamTurn().equals(userColor)) {
//            sendError(session, new ErrorMessage("Error: Not your turn"));
//            return;
//        }
//
//        try {
//            game.game().makeMove(command.getMove());
//        }catch (InvalidMoveException ex) {
//            sendError(session, new ErrorMessage("Error: Invalid move"));
//            return;
//        }
//
//        try {
//            Server.gameService.updateGame(authToken, game);
//        }catch (DataAccessException ex) {
//            sendError(session, new ErrorMessage("Error: Unable to persist game"));
//            return;
//        }
//
//        LoadMessage load = new LoadMessage(game.game());
//        broadcastMessage(gameID, load);
//
//        NotificationMessage moveNotif = new NotificationMessage(String.format("%s is moved", auth.username()));
//        broadcastMessage(gameID, moveNotif);
//
//        ChessGame.TeamColor enemyColor = userColor == ChessGame.TeamColor.WHITE
//                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
//        String player = enemyColor == ChessGame.TeamColor.WHITE
//                ? game.whiteUsername() : game.blackUsername();
//        if(game.game().isInCheck(enemyColor)) {
//            NotificationMessage checkNotif = new NotificationMessage(String.format("%s is in check", player));
//            broadcastMessage(gameID, checkNotif);
//        }
//
//        if(game.game().isInCheck(enemyColor)) {
//            game.game().setGameOver(true);
//            try {
//                Server.gameService.updateGame(authToken, game);
//            }catch (DataAccessException ex) {
//                //ignore
//            }
//
//            NotificationMessage checkMateNotif = new NotificationMessage(String.format("%s is in checkmate", player));
//            broadcastMessage(gameID, checkMateNotif);
//        }
//    }
//
//    private void handleResign(Session session, ResignCommand command) {
//        String authToken = command.getAuthToken();
//        int gameID = command.getGameID();
//
//        AuthData auth;
//        try {
//            auth = Server.userService.getAuth(authToken);
//        }catch (DataAccessException ex) {
//            sendError(session, new ErrorMessage("Error: Unauthorized"));
//            return;
//        }
//
//        GameData game;
//        try {
//            game = Server.gameService.getGameData(authToken, gameID);
//        }catch (DataAccessException ex) {
//            sendError(session, new ErrorMessage("Error: Invalid game"));
//            return;
//        }
//
//        ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
//        if(userColor == null) {
//            sendError(session, new ErrorMessage("Error: Observing game"));
//            return;
//        }
//
//        if(game.game().getGameOver()) {
//            sendError(session, new ErrorMessage("Error: Game is over"));
//            return;
//        }
//
//        game.game().setGameOver(true);
//        try {
//            Server.gameService.updateGame(authToken, game);
//        }catch (DataAccessException ex) {
//            //ignore
//        }
//        String enemy = userColor == ChessGame.TeamColor.WHITE
//                ? game.blackUsername() : game.whiteUsername();
//        NotificationMessage notification = new NotificationMessage(String.format("%s has forfeited, %s wins!", auth.username(), enemy));
//        broadcastMessage(gameID, notification);
//
//        session.close();
//    }
//
//    private void handleLeave(Session session, LeaveCommand command) {
//        String authToken = command.getAuthToken();
//        int gameID = command.getGameID();
//
//        AuthData auth;
//        try {
//            auth = Server.userService.getAuth(authToken);
//        }catch (DataAccessException ex) {
//            sendError(session, new ErrorMessage("Error: Unauthorized"));
//            return;
//        }
//
//        NotificationMessage leaveNotif = new NotificationMessage(
//                String.format("%s left the game", auth.username()));
//        broadcastMessage(gameID, leaveNotif);
//
//        session.close();
//    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Integer gameID = gameSessions.remove(session);
        if(gameID != null) {
            inGameSessions.getOrDefault(gameID, Set.of()).remove(session);
        }
        authSessions.remove(session);
        System.out.println("WebSocket close: " + reason);
    }

    private void sendToGame(int gameID, ServerMessage message) {
        for(Session s: inGameSessions.getOrDefault(gameID, Set.of())) {
            if(s.isOpen()) {
                send(s, message);
            }
        }
    }

    private void send(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        }catch (IOException e) {
            System.err.println("Send error: " + e.getMessage());
        }
    }

    private void sendError(Session session, ErrorMessage errorMessage) {
        try {
            session.getRemote().sendString(gson.toJson(errorMessage));
        }catch (Exception e) {
             //ignore
        }
    }

//    private ChessGame.TeamColor getTeamColor(String username, GameData game) {
//        if(username.equals(game.whiteUsername())) {
//            return ChessGame.TeamColor.WHITE;
//        }else if(username.equals(game.blackUsername())) {
//            return ChessGame.TeamColor.BLACK;
//        }else {
//            return null;
//        }
//    }
}
