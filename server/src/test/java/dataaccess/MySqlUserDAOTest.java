package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySqlUserDAOTest {
    private MySqlUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        userDAO.createTable();
        userDAO.clear();
    }

    @Test
    void createTablePositive() {
        assertDoesNotThrow(() -> userDAO.createTable());
    }

    @Test
    void createUserPositive() throws DataAccessException {
        UserData user =  new UserData("milly", "password1", "chess1@example.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("milly");
        assertNotNull(result);
        assertEquals("milly", result.username());
    }

    @Test
    void createUserNegative() throws DataAccessException {
        UserData user =  new UserData("jason", "password2", "chess2@example.com");
        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }

    @Test
    void getUserPositive() throws DataAccessException {
        UserData user =  new UserData("charlie", "password3", "chess3@example.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("charlie");
        assertNotNull(result);
        assertEquals("charlie", result.username());
    }

    @Test
    void getUserNegative() throws DataAccessException {
        UserData result = userDAO.getUser("random user");
        assertNull(result);
    }

    @Test
    void clearPositive() throws DataAccessException {
        UserData user =  new UserData("julia", "password4", "chess4@example.com");
        userDAO.createUser(user);

        userDAO.clear();
        assertNull(userDAO.getUser("julia"));
    }

    @Test
    void verifyUserPositive() throws DataAccessException {
        UserData user =  new UserData("michael", "password5", "chess5@example.com");
        userDAO.createUser(user);

        assertTrue(userDAO.verifyUser("michael", "password5"));
    }

    @Test
    void verifyUserNegative() throws DataAccessException {
        UserData user =  new UserData("wendy", "password6", "chess5@example.com");
        userDAO.createUser(user);

        assertFalse(userDAO.verifyUser("wendy", "invalid"));
    }
}