package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        String serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(serverUrl);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() {
        facade.clearServer();
    }

    @Test
    public void registerPositive() {
        AuthData auth = facade.register("tester1", "password1", "tester1@example.com");
        assertNotNull(auth);
        assertEquals("tester1", auth.username());
    }

    @Test
    public void registerNegative() {
        assertThrows(Exception.class, () -> facade.register(null, "password2", "tester2@example.com"));
    }

    @Test
    public void loginPositive() {
        facade.register("tester3", "password3", "tester3example.com");
        AuthData auth = facade.login("tester3", "password3");
        assertNotNull(auth);
        assertEquals("tester3", auth.username());
    }

    @Test
    public void loginNegative() {
        assertThrows(Exception.class, () -> facade.login("random", "invalid"));
    }

    @Test
    public void logoutPositive() {
        AuthData auth = facade.register("tester4", "password4", "tester4@example.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutNegative() {
        assertThrows(Exception.class, () -> facade.logout("invalid"));
    }

    @Test
    public void createGamePositive() {
        AuthData auth = facade.register("tester5", "password5", "tester5@example.com");
        GameData game = facade.createGame("game1", auth.authToken());
        assertNotNull(game);
        assertEquals("game1", game.gameName());
    }

    @Test
    public void createGameNegative() {
        assertThrows(Exception.class, () -> facade.createGame("game", "invalid token"));
    }

    @Test
    public void listGamesPositive() {
        AuthData auth = facade.register("tester6", "password6", "tester6@example.com");
        facade.createGame("game2", auth.authToken());
        List<GameData> games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertTrue(games.size() >= 1);
    }
}
