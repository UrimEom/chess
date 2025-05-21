package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private GameService gameService;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private String token;
    private String username = "tester";

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO, authDAO);

        AuthData auth = new AuthData("coin1", username);
        authDAO.createAuth(auth);
        token = auth.authToken();
    }

    @Test
    void createGamePositive() throws DataAccessException {
        GameData game = gameService.createGame("chess1", token);
        assertNotNull(game);
        assertEquals("chess1", game.gameName());
    }

    @Test
    void createGameNegative() {
        DataAccessException e = assertThrows(DataAccessException.class, () -> gameService.createGame(null, token));
        assertEquals("Error: bad request", e.getMessage());
    }

    @Test
    void joinGamePositive() throws DataAccessException {
        int gameID = gameService.createGame("my chess", token).gameID();
        gameService.joinGame(gameID, "white", token);

        GameData game = gameDAO.getGame(gameID);
        assertEquals(username, game.whiteUsername());
    }

    @Test
    void joinGameNegative() {
        DataAccessException e = assertThrows(DataAccessException.class, () -> gameService.joinGame(2,"black", "random"));
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        gameService.createGame("chess1", token);
        gameService.createGame("chess2", token);
        gameService.createGame("chess3", token);

        Collection<GameData> games = gameService.listGames(token);
        assertEquals(3, games.size());
    }

    @Test
    void listGamesNegative() {
        DataAccessException e = assertThrows(DataAccessException.class, () -> gameService.listGames("random"));
        assertEquals("Error: unauthorized", e.getMessage());
    }
}