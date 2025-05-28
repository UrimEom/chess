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

public class LogoutHandler {
    private final UserService userService;
    private final AuthDAO authDAO;
    private final Gson gson = new Gson();

    public LogoutHandler(UserService userService, AuthDAO authDAO) {
        this.userService = userService;
        this.authDAO = authDAO;
    }

    public String handler(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");

            if(authToken == null || authDAO.getAuth(authToken) == null) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            userService.logout(authToken);

            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: server error"));
        }

    }

}
