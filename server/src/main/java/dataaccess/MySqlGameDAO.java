package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {

    public void createTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS games (
               `id` INT NOT NULL AUTO_INCREMENT,
               `whiteUsername` VARCHAR(255) NULL,
               `blackUsername` VARCHAR(255) NULL,
               `gameName` VARCHAR(255) NOT NULL,
               `gameData` TEXT NULL,
               PRIMARY KEY (`id`));
               """;
            try (var preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.executeUpdate();
            }

        }catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database", ex);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, gameData) VALUES (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, game.whiteUsername());
                statement.setString(2, game.blackUsername());
                statement.setString(3, game.gameName());
                statement.setString(4, serializeGame(game.game()));

                statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if(rs.next()) {
                        return rs.getInt(1);
                    }else {
                        throw new DataAccessException("Game ID was not created.");
                    }
                }
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to insert game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE id = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.setInt(1, gameID);
                try (var rs = statement.executeQuery()) {
                    if(rs.next()) {
                        return new GameData(rs.getInt("id"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                deserializeGame(rs.getString("gameData")));
                    }else {
                        return null;
                    }
                }
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to get game", ex);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String sql = "SELECT * FROM games";
        Collection<GameData> games = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection(); var statement = conn.prepareStatement(sql); var rs= statement.executeQuery()) {
            while(rs.next()) {
                games.add(new GameData(rs.getInt("id"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        deserializeGame(rs.getString("gameData"))));
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to list games", ex);
        }

        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameData = ? WHERE id = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1, game.whiteUsername());
                statement.setString(2, game.blackUsername());
                statement.setString(3, game.gameName());
                statement.setString(4, serializeGame(game.game()));
                statement.setInt(5, game.gameID());

                int updated = statement.executeUpdate();
                if(updated == 0) {
                    throw new DataAccessException("No game found with ID: " + game.gameID());
                }
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to update game", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        }catch(SQLException ex) {
            throw new DataAccessException("Unable to clear games", ex);
        }
    }

    private String serializeGame(ChessGame game) {
        return new Gson().toJson(game);
    }

    private ChessGame deserializeGame(String json) {
        return new Gson().fromJson(json, ChessGame.class);
    }
}
