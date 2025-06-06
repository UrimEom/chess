package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(String username, String password, String email) throws DataAccessException {
        if(username == null || password == null || email == null) {
            throw new DataAccessException("Error: bad request");
        }

        if(userDAO.getUser(username) != null) {
            throw new DataAccessException("Error: already taken");
        }

        UserData user = new UserData(username, password, email);
        userDAO.createUser(user);

        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        authDAO.createAuth(auth);

        return auth;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        if(username == null || password == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData user = userDAO.getUser(username);
        if(user == null || !BCrypt.checkpw(password, user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        authDAO.createAuth(auth);

        return auth;
    }

    public void logout(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if(auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        authDAO.deleteAuth(authToken);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if(auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return authDAO.getAuth(authToken);
    }
}
