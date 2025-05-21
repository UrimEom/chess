package server;

import dataaccess.*;
import spark.*;

import java.util.List;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", new RegisterHandler(userDAO, authDAO)::handler);
        Spark.post("/session", new LoginHandler(userDAO, authDAO)::handler);
        Spark.delete("/session", new LogoutHandler(userDAO, authDAO)::handler);
        Spark.get("/game", new ListHandler(gameDAO, authDAO)::handler);
        Spark.post("/game", new CreateHandler(gameDAO, authDAO)::handler);
        Spark.put("/game", new JoinHandler(gameDAO, authDAO)::handler);
        Spark.delete("/db", new ClearHandler(userDAO, authDAO, gameDAO)::handler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
