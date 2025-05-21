package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {

    @Test
    void clearPositive() throws DataAccessException {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        userDAO.createUser(new UserData("kim", "sunny", "kim@example.com"));
        authDAO.createAuth(new AuthData("token1", "kim"));
        gameDAO.createGame(new GameData(1, null, null, "test chess", null));

        assertNotNull(userDAO.getUser("kim"));
        assertNotNull(authDAO.getAuth("token1"));
        assertEquals(1, gameDAO.listGames().size());

        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        clearService.clear();

        assertNull(userDAO.getUser("kim"));
        assertNull(authDAO.getAuth("token1"));
        assertEquals(0, gameDAO.listGames().size());
    }
}