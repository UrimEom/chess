package ui;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    //register
    //login
    //logout

    //create game
    //list game
    //join game
    //observe game
    //print board

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