package dataaccess;

import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO {

    public MySqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1, user.username());
                statement.setString(2, user.password());
                statement.setString(3, user.email());
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM user WHERE username = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet rs = statement.executeQuery()) {
                    if(rs.next()) {
                        return new UserData(rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email"));
                    }else {
                        return null;
                    }
                }
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to get user", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to clear users table", ex);
        }
    }

    private final String[] createStatements =  {
            """
                CREATE TABLE IF NOT EXISTS users (
                id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255));
                """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for(var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        }catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database: %s", ex);
        }
    }
}