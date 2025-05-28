package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class MySqlGameDAOTest {
    private MySqlGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new MySqlGameDAO();
        gameDAO.createTable();
        gameDAO.clear();
    }

    @Test
    void createTablePositive() {
        assertDoesNotThrow(() -> gameDAO.createTable());
    }

    @Test
    void createGamePositive() throws DataAccessException {
        GameData game1 = new GameData(0, null, null, "chess1", new ChessGame());
        int id = gameDAO.createGame(game1);

        assertTrue(id > 0);
    }

    @Test
    void createGameNegative() {
        GameData game2 = new GameData(0, null, null, null, new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(game2));
    }

    @Test
    void getGamePositive() throws DataAccessException {
        GameData game3 = new GameData(0, "white", "black", "chess2", new ChessGame());
        int id = gameDAO.createGame(game3);
        GameData result = gameDAO.getGame(id);

        assertNotNull(result);
        assertEquals("chess2", result.gameName());
    }

    @Test
    void getGameNegative() throws DataAccessException {
        assertNull(gameDAO.getGame(350));
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        GameData game4 = new GameData(0, null, null, "chess3", new ChessGame());
        GameData game5 = new GameData(0, null, null, "chess4", new ChessGame());
        gameDAO.createGame(game4);
        gameDAO.createGame(game5);

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            try(var statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS games");
            }
        }catch (SQLException e) {
            throw new DataAccessException("Unable to access databse", e);
        }

        assertThrows(DataAccessException.class, () -> gameDAO.listGames());
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        GameData original = new GameData(0, null, null, "first", new ChessGame());
        int id = gameDAO.createGame(original);

        GameData updated = new GameData(id, "white", null, "update", new ChessGame());
        gameDAO.updateGame(updated);

        GameData result = gameDAO.getGame(id);
        assertEquals("white", result.whiteUsername());
        assertEquals("update", result.gameName());
    }

    @Test
    void updateGameNegative() {
        GameData game = new GameData(400, "white", "black", "invalid", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(game));
    }

    @Test
    void clearPositive() throws DataAccessException {
        GameData newGame = new GameData(0, null, null, "new", new ChessGame());
        gameDAO.createGame(newGame);
        gameDAO.clear();

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(0, games.size());
    }
}