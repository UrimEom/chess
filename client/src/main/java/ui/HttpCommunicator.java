package ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpCommunicator {
    private final String serverUrl;
    private final Gson gson = new Gson();
    private final ServerFacade facade;

    public HttpCommunicator(ServerFacade facade, String serverUrl) {
        this.facade = facade;
        this.serverUrl = serverUrl;
    }

    //register
    public AuthData register(String username, String password, String email) {
        Map<String, String> request = Map.of("username", username, "password", password, "email", email);
        AuthData auth = makeRequest("POST", "/user", request, AuthData.class, null);

        if(auth != null && auth.authToken() != null) {
            facade.setAuthToken(auth.authToken());
        }
        return auth;
    }

    //login
    public AuthData login(String username, String password) {
        Map<String, String> request = Map.of("username", username, "password", password);
        AuthData auth = makeRequest("POST", "/session", request, AuthData.class, null);

        if(auth != null && auth.authToken() != null) {
            facade.setAuthToken(auth.authToken());
        }
        return auth;
    }

    //logout
    public void logout(String authToken) {
        makeRequest("DELETE", "/session", null, Void.class, authToken);
        facade.setAuthToken(null);
        //maybe add logic for clearing the authToken?
    }

    //create game
    public GameData createGame(String gameName, String authToken) {
        Map<String, String> request = Map.of("gameName", gameName);
        return makeRequest("POST", "/game", request, GameData.class, authToken);
    }

    //list game
    public List<GameData> listGames(String authToken) {
        String response = makeRequest("GET", "/game", null, String.class, authToken);

        JsonObject json = gson.fromJson(response, JsonObject.class);
        JsonArray gameArray = json.getAsJsonArray("games");

        GameData[] games = gson.fromJson(gameArray, GameData[].class);

        return Arrays.asList(games);
    }

    //join game
    public void joinGame(int gameID, String playerColor, String authToken) {
        Map<String, Object> request = new HashMap<>();
        request.put("gameID", gameID);
        if(playerColor != null) {
            request.put("playerColor", playerColor);
        }
        makeRequest("PUT", "/game", request, Void.class, authToken);
    }

    public void clearServer() {
        makeRequest("DELETE", "/db", null, Void.class, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) {
        try {
            URL url = new URL(serverUrl + path);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setRequestProperty("Accept", "application/json");

            if (authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            if (request != null) {
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/json");
                try (OutputStream osReq = http.getOutputStream(); OutputStreamWriter writer = new OutputStreamWriter(osReq, StandardCharsets.UTF_8)) {
                    gson.toJson(request, writer);
                    writer.flush();
                }
            }

            if (http.getResponseCode() == 200) {
                try (InputStream body = http.getInputStream(); InputStreamReader reader = new InputStreamReader(body)) {
                    if (responseClass == String.class) {
                        return responseClass.cast(new BufferedReader(reader).readLine());
                    } else if (responseClass == Void.class) {
                        return null;
                    }
                    return gson.fromJson(reader, responseClass);
                }
            } else {
                try (InputStream body = http.getErrorStream(); InputStreamReader reader = new InputStreamReader(body)) {
                    throw new RuntimeException(new BufferedReader(reader).readLine());
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException("HTTP request was failed: " + ex.getMessage());
        }
    }
}
