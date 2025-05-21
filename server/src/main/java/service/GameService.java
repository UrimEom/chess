package service;


import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public GameData createGame(String gameName, String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if(auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if(gameName == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData newGame = new GameData(0, null, null, gameName, null);
        int gameID = gameDAO.createGame(newGame);

        return gameDAO.getGame(gameID);
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if(auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if(game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String username = auth.username();
        String whitePlayer = game.whiteUsername();
        String blackPlayer = game.blackUsername();

        if(playerColor.equalsIgnoreCase("WHITE")) {
            if(whitePlayer != null) {
                throw new DataAccessException("Error: already taken");
            }
            whitePlayer = username;
        }else if(playerColor.equalsIgnoreCase("BLACK")) {
            if(blackPlayer != null) {
                throw new DataAccessException("Error: already taken");
            }
            blackPlayer = username;
        }else {
            throw new DataAccessException("Error: Access Error");
        }

        GameData updatedData = new GameData(game.gameID(), whitePlayer, blackPlayer, game.gameName(), game.game());

        gameDAO.updateGame(updatedData);
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if(auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return gameDAO.listGames();
    }

}
