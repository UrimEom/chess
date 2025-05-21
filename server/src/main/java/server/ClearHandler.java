package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import spark.Request;
import spark.Response;

import java.util.Map;

public class ClearHandler {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Gson gson = new Gson();

    public ClearHandler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public String handler(Request req, Response res) {
        try {
            userDAO.clear();
            authDAO.clear();
            gameDAO.clear();

            res.status(200);
            return "{}";

        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: sever error"));
        }
    }
}
