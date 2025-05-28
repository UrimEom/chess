package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO {

    public void createTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                `id` INT NOT NULL AUTO_INCREMENT,
                `username` VARCHAR(255) NOT NULL,
                `password` VARCHAR(255) NOT NULL,
                `email` VARCHAR(255) NULL,
                PRIMARY KEY (`id`),
                UNIQUE (username)
                );
                """;
            try (var preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.executeUpdate();
            }

        }catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database", ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1, user.username());
                String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
                statement.setString(2, hashedPassword);
                statement.setString(3, user.email());
                statement.executeUpdate();
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

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
            throw new DataAccessException("Unable to clear users", ex);
        }
    }

    public boolean verifyUser(String username, String providedClearTextPassword) throws DataAccessException {
        UserData user = getUser(username);
        if(user == null) {
            return false;
        }
        return BCrypt.checkpw(providedClearTextPassword, user.password());
    }
}