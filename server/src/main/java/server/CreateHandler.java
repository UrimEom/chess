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

public class CreateHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateHandler(GameDAO gameDAO, AuthDAO authDAO) {
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

            if(!json.has("gameName") || json.get("gameName").getAsString().isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            String gameName = json.get("gameName").getAsString();

            int gameID = gameService.createGame(gameName, authToken).gameID();

            res.status(200);
            return gson.toJson(Map.of("gameID", gameID));

        } catch (DataAccessException e) {
            if ("Error: bad request".equals(e.getMessage())) {
                res.status(400);
            } else if ("Error: unauthorized".equals(e.getMessage())) {
                res.status(401);
            } else {
                res.status(500);
            }
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: server error"));
        }
    }
}
