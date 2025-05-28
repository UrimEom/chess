package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySqlAuthDAOTest {
    private MySqlAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        authDAO.createTable();
        authDAO.clear();
    }

    @Test
    void createTablePositive() {
        assertDoesNotThrow(() -> authDAO.createTable());
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token1", "user1");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("token1");
        assertNotNull(result);
        assertEquals("user1", result.username());
    }

    @Test
    void createAuthNegative() throws DataAccessException {
        AuthData auth = new AuthData("token2", "user2");
        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    @Test
    void getAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token3", "user3");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("token3");
        assertNotNull(result);
        assertEquals("user3", result.username());
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        assertNull(authDAO.getAuth("random"));
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token4", "user4");
        authDAO.createAuth(auth);
        authDAO.deleteAuth("token4");

        assertNull(authDAO.getAuth("token4"));
    }

    @Test
    void deleteAuthNegative() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("missing"));
    }

    @Test
    void clearPositive() throws DataAccessException {
        AuthData auth = new AuthData("token5", "user5");
        authDAO.createAuth(auth);

        authDAO.clear();
        assertNull(authDAO.getAuth("token5"));
    }
}