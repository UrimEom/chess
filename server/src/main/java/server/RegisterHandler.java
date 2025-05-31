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

public class RegisterHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.userService = new UserService(userDAO, authDAO);
    }

    public String handler(Request req, Response res) {
        try {
            String body = req.body();
            if(body == null || body.isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: request body was missing"));
            }

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            if(!json.has("username") ||
                    !json.has("password") ||
                    !json.has("email") ||
                    json.get("username").getAsString().isBlank() ||
                    json.get("password").getAsString().isBlank() ||
                    json.get("email").getAsString().isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();
            String email = json.get("email").getAsString();

            AuthData auth = userService.register(username, password, email);

            res.status(200);
            return gson.toJson(auth);

        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                res.status(400);
            } else if (e.getMessage().equals("Error: already taken")) {
                res.status(403);
            } else {
                res.status(500);
            }
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: server error"));
        }

    }
}
