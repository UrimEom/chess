package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    //register
    public AuthData register(String username, String password, String email) {
        UserData user = new UserData(username, password, email);
        return makeRequest("POST", "/user", user, AuthData.class, null);
    }

    //login
    public AuthData login(String username, String password) {
        UserData user = new UserData(username, password, null);
        return makeRequest("POST", "/session", user, AuthData.class, null);
    }

    //logout
    public void logout(String authToken) {
        makeRequest("DELETE", "/session", null, Void.class, authToken);
    }

    //create game
    public GameData createGame(String gameName, String authToken) {
        Map<String, String> request = Map.of("gameName", gameName);
        return makeRequest("POST", "/game", request, GameData.class, authToken);
    }

    //list game
    public List<GameData> listGames(String authToken) {
        GameData[] games = makeRequest("GET", "/game", null, GameData[].class, authToken);

        if(games == null) {
            return null;
        }
        return Arrays.asList(games);
    }

    //join game
    public void joinGame(int gameID, String playerColor, String authToken) {
        Map<String, Object> request = new HashMap<>();
        request.put("gameID", gameID);
        request.put("playerColor", playerColor);
        makeRequest("PUT", "/game", request, Void.class, authToken);
    }

    //observe game
    public void observeGame(int gameID, String authToken) {
        joinGame(gameID, null, authToken);
    }

    //print board
    public void clearServer() {
        makeRequest("DELETE", "/db", null, Void.class, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) {
        try {
            URL url = new URL(serverUrl + path);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setRequestProperty("Accept", "application/json");

            if(authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            if(request != null) {
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/json");
                try (OutputStream osReq = http.getOutputStream()) {
                    gson.toJson(request, new OutputStreamWriter(osReq));
                }
            }

            if(http.getResponseCode() >= 400) {
                try (InputStream error = http.getErrorStream()) {
                    String errorMessage = new BufferedReader(new InputStreamReader(error)).readLine();
                    throw new RuntimeException("Error: " + errorMessage);
                }
            }

            if(responseClass == null || responseClass == Void.class) return null;

            try(InputStream input = http.getInputStream()) {
                return new Gson().fromJson(new InputStreamReader(input), responseClass);
            }
        }catch (IOException ex) {
            throw new RuntimeException("HTTP request was failed: " + ex.getMessage());
        }
    }

}