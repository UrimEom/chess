package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.Map;

public class ListHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameService = new GameService(gameDAO, authDAO);
    }

    public String handler(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if(authToken == null) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            Collection<GameData> games = gameService.listGames(authToken);

            res.status(200);
            return gson.toJson(Map.of("games", games));

        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: sever error"));
        }
    }
}
