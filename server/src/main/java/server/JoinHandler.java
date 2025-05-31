package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Map;

public class JoinHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameService = new GameService(gameDAO, authDAO);
    }

    public String handler(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if(authToken == null) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();

            if(!json.has("gameID") || json.get("gameID").isJsonNull()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            int gameID = json.get("gameID").getAsInt();

            if(!json.has("playerColor")) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }
            if(json.get("playerColor").isJsonNull()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            String playerColor = json.get("playerColor").getAsString();
            if (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK") && !playerColor.equalsIgnoreCase("OBSERVER")) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }
            gameService.joinGame(gameID, playerColor, authToken);

            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                res.status(400);
            } else if (e.getMessage().equals("Error: unauthorized")) {
                res.status(401);
            } else if (e.getMessage().equals("Error: already taken")) {
                res.status(403);
            } else {
                res.status(500);
            }
            String message = e.getMessage();
            if(!message.startsWith("Error: ")) {
                message = "Error: " + e.getMessage();
            }
            return gson.toJson(Map.of("message", message));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: server error"));
        }
    }
}
