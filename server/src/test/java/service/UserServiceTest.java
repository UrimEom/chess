package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setup() {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    //register tests
    @Test
    void registerPositive() throws DataAccessException {
        AuthData auth = userService.register("grace", "flower1", "grace@example.com");
        assertNotNull(auth.authToken());
        assertEquals("grace", auth.username());
    }

    @Test
    void registerNegative() {
        DataAccessException e = assertThrows(DataAccessException.class, () -> userService.register(null, "flower2", "grace@example.com"));
        assertEquals("Error: bad request", e.getMessage());
    }

    @Test
    void loginPositive() throws DataAccessException {
        userService.register("brian", "mermaid1", "brian@example.com");
        AuthData auth = userService.login("brian", "mermaid1");
        assertNotNull(auth.authToken());
        assertEquals("brian", auth.username());
    }

    @Test
    void loginNegative() {
        DataAccessException e = assertThrows(DataAccessException.class, () -> userService.login("brian", "merma"));
        assertEquals("Error: unauthorized", e.getMessage());
    }

    @Test
    void logoutPositive() throws DataAccessException {
        AuthData auth = userService.register("paul", "mickey1", "paul@example.com");

        userService.logout(auth.authToken());
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void logoutNegative() {
        DataAccessException e = assertThrows(DataAccessException.class, () -> userService.logout("random"));
        assertEquals("Error: unauthorized", e.getMessage());
    }
}