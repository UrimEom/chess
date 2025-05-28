package server;

import dataaccess.*;
import org.eclipse.jetty.server.Authentication;
import service.UserService;
import spark.*;

import java.util.List;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        try {
            DatabaseManager.createDatabase();

            MySqlUserDAO userDAO = new MySqlUserDAO();
            MySqlAuthDAO authDAO = new MySqlAuthDAO();
            MySqlGameDAO gameDAO = new MySqlGameDAO();
            userDAO.createTable();
            authDAO.createTable();
            gameDAO.createTable();

            UserService userService = new UserService(userDAO, authDAO);

            // Register your endpoints and handle exceptions here.
            Spark.post("/user", new RegisterHandler(userDAO, authDAO)::handler);
            Spark.post("/session", new LoginHandler(userDAO, authDAO)::handler);
            Spark.delete("/session", new LogoutHandler(userService, authDAO)::handler);
            Spark.get("/game", new ListHandler(gameDAO, authDAO)::handler);
            Spark.post("/game", new CreateHandler(gameDAO, authDAO)::handler);
            Spark.put("/game", new JoinHandler(gameDAO, authDAO)::handler);
            Spark.delete("/db", new ClearHandler(userDAO, authDAO, gameDAO)::handler);
        }catch (Exception e) {
            throw new RuntimeException("Server setup failed", e);
        }

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
