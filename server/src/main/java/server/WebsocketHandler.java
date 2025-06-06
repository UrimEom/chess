package server;

import chess.ChessGame;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketHandler {
    private static final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Session>> inGameSessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        gameSessions.put(session, 0);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        gameSessions.remove(session);
    }

   @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        System.out.printf("Received: %s\n", message);


        if(message.contains("\"commandType\":\"JOIN_PLAYER\"")) {
            JoinPlayerCommand command = gson.fromJson(message, JoinPlayerCommand.class);
            gameSessions.replace(session, command.getGameID());
            handleJoinPlayer(session, command);
        }else if(message.contains("\"commandType\":\"JOIN_OBSERVER\"")) {
            JoinObserverCommand command = gson.fromJson(message, JoinObserverCommand.class);
            gameSessions.replace(session, command.getGameID());
            handleJoinObserver(session, command);
        }else if(message.contains("\"commandType\":\"MAKE_MOVE\"")) {
            MakeMoveCommand command = gson.fromJson(message, MakeMoveCommand.class);
            handleMakeMove(session, command);
        }else if(message.contains("\"commandType\":\"LEAVE\"")) {
            LeaveCommand command = gson.fromJson(message, LeaveCommand.class);
            handleLeave(session, command);
        }else if(message.contains("\"commandType\":\"RESIGN\"")) {
            ResignCommand command = gson.fromJson(message, ResignCommand.class);
            handleResign(session, command);
        }else {
            sendError(session, new ErrorMessage("Error: Invalid command type"));
        }
    }

    private void handleJoinPlayer(Session session, JoinPlayerCommand command) throws IOException {
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();
        String colorStr = command.getColor().toString();

        AuthData auth;
        try {
            auth = Server.userService.getAuth(authToken);
        }catch(DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unauthorized"));
            return;
        }

        GameData game;
        try {
            game = Server.gameService.getGameData(authToken, gameID);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Invalid game"));
            return;
        }

        ChessGame.TeamColor chosenColor = colorStr.equalsIgnoreCase("WHITE")
                ? ChessGame.TeamColor.WHITE
                : ChessGame.TeamColor.BLACK;

        boolean correctColor;
        if(chosenColor == ChessGame.TeamColor.WHITE) {
            correctColor = Objects.equals(game.whiteUsername(), auth.username());
        }else {
            correctColor = Objects.equals(game.blackUsername(), auth.username());
        }

        if(!correctColor) {
            sendError(session, new ErrorMessage("Error: Joined with wrong color"));
            return;
        }

        LoadMessage load = new LoadMessage(game.game());
        sendMessage(session, load);

        NotificationMessage notification = new NotificationMessage(
                String.format("%s has joined the game as %s", auth.username(), colorStr));
        broadcastMessage(gameID, notification);
    }

    private void handleJoinObserver(Session session, JoinObserverCommand command) {
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();

        AuthData auth;
        try {
            auth = Server.userService.getAuth(authToken);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unauthorized"));
            return;
        }

        GameData game;
        try {
            game = Server.gameService.getGameData(authToken, gameID);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Invalid game"));
            return;
        }

        LoadMessage load = new LoadMessage(game.game());
        sendMessage(session, load);

        NotificationMessage notification = new NotificationMessage(
                String.format("%s has joined the game as observer", auth.username()));
        broadcastMessage(gameID, notification);
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        AuthData auth;
        try {
            auth = Server.userService.getAuth(authToken);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unauthorized"));
            return;
        }

        GameData game;
        try {
            game = Server.gameService.getGameData(authToken, gameID);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Invalid game"));
            return;
        }

        ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
        if(userColor == null) {
            sendError(session, new ErrorMessage("Error: Observing game"));
            return;
        }

        if(game.game().getGameOver()) {
            sendError(session, new ErrorMessage("Error: Game is over"));
            return;
        }

        if(!game.game().getTeamTurn().equals(userColor)) {
            sendError(session, new ErrorMessage("Error: Not your turn"));
            return;
        }

        try {
            game.game().makeMove(command.getMove());
        }catch (InvalidMoveException ex) {
            sendError(session, new ErrorMessage("Error: Invalid move"));
            return;
        }

        try {
            Server.gameService.updateGame(authToken, game);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unable to persist game"));
            return;
        }

        LoadMessage load = new LoadMessage(game.game());
        broadcastMessage(gameID, load);

        NotificationMessage moveNotif = new NotificationMessage(String.format("%s is moved", auth.username()));
        broadcastMessage(gameID, moveNotif);

        ChessGame.TeamColor enemyColor = userColor == ChessGame.TeamColor.WHITE
                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        String player = enemyColor == ChessGame.TeamColor.WHITE
                ? game.whiteUsername() : game.blackUsername();
        if(game.game().isInCheck(enemyColor)) {
            NotificationMessage checkNotif = new NotificationMessage(String.format("%s is in check", player));
            broadcastMessage(gameID, checkNotif);
        }

        if(game.game().isInCheck(enemyColor)) {
            game.game().setGameOver(true);
            try {
                Server.gameService.updateGame(authToken, game);
            }catch (DataAccessException ex) {
                //ignore
            }

            NotificationMessage checkMateNotif = new NotificationMessage(String.format("%s is in checkmate", player));
            broadcastMessage(gameID, checkMateNotif);
        }
    }

    private void handleResign(Session session, ResignCommand command) {
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();

        AuthData auth;
        try {
            auth = Server.userService.getAuth(authToken);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unauthorized"));
            return;
        }

        GameData game;
        try {
            game = Server.gameService.getGameData(authToken, gameID);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Invalid game"));
            return;
        }

        ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
        if(userColor == null) {
            sendError(session, new ErrorMessage("Error: Observing game"));
            return;
        }

        if(game.game().getGameOver()) {
            sendError(session, new ErrorMessage("Error: Game is over"));
            return;
        }

        game.game().setGameOver(true);
        try {
            Server.gameService.updateGame(authToken, game);
        }catch (DataAccessException ex) {
            //ignore
        }
        String enemy = userColor == ChessGame.TeamColor.WHITE
                ? game.blackUsername() : game.whiteUsername();
        NotificationMessage notification = new NotificationMessage(String.format("%s has forfeited, %s wins!", auth.username(), enemy));
        broadcastMessage(gameID, notification);

        session.close();
    }

    private void handleLeave(Session session, LeaveCommand command) {
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();

        AuthData auth;
        try {
            auth = Server.userService.getAuth(authToken);
        }catch (DataAccessException ex) {
            sendError(session, new ErrorMessage("Error: Unauthorized"));
            return;
        }

        NotificationMessage leaveNotif = new NotificationMessage(
                String.format("%s left the game", auth.username()));
        broadcastMessage(gameID, leaveNotif);

        session.close();
    }

    private void sendMessage(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        }catch (Exception e) {
            //ignore
        }
    }

    private void sendError(Session session, ErrorMessage errorMessage) {
        try {
            session.getRemote().sendString(gson.toJson(errorMessage));
        }catch (Exception e) {
             //ignore
        }
    }

    private void broadcastMessage(int gameID, ServerMessage message) {
        Set<Session> joiners = inGameSessions.get(gameID);
        if(joiners == null) { return; }

        String json = gson.toJson(message);
        for(Session s : joiners) {
            try {
                s.getRemote().sendString(json);
            } catch (Exception e) {
                //ignore
            }
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
