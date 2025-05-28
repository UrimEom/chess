package dataaccess;

import model.AuthData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO {

    public void createTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                `authToken` VARCHAR(255) NOT NULL,
                `username` VARCHAR(255) NOT NULL,
                PRIMARY KEY (`authToken`));
                """;
            try (var preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.executeUpdate();
            }

        }catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database", ex);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
               statement.setString(1, auth.authToken());
               statement.setString(2, auth.username());
               statement.executeUpdate();
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to insert auth token", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auth_tokens WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1, authToken);
                try (ResultSet rs = statement.executeQuery()) {
                    if(rs.next()) {
                        return new AuthData(rs.getString("authToken"),
                                rs.getString("username"));
                    }else {
                        return null;
                    }
                }
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to get auth token", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth_tokens WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1, authToken);
                statement.executeUpdate();
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to delete auth token", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth_tokens";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to clear auth_tokens table", ex);
        }
    }
}
