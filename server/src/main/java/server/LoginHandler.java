package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import service.UserService;
import spark.Request;
import spark.Response;

import java.util.Map;

public class LoginHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.userService = new UserService(userDAO, authDAO);
    }

    public String handler(Request req, Response res) {
        try {
            JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();

            if(!json.has("username") ||
                    !json.has("password") ||
                    json.get("username").getAsString().isBlank() ||
                    json.get("password").getAsString().isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();

            AuthData auth = userService.login(username, password);

            res.status(200);
            return gson.toJson(auth);

        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                res.status(400);
            } else if (e.getMessage().equals("Error: unauthorized")) {
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
