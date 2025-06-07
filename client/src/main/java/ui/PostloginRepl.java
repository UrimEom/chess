package ui;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.out;

public class PostloginRepl {
    private final ServerFacade server;
    private final AuthData auth;
    private final Scanner scanner = new Scanner(System.in);
    private List<GameData> gameLists = new ArrayList<>();

    public PostloginRepl(ServerFacade server, AuthData auth) {
        this.server = server;
        this.auth = auth;
    }

    public void run() {
        System.out.println("Logged in as " + auth.username());

        boolean running = true;
        while(running) {
            System.out.println("\n[LOGGED_IN] >>> ");
            String input = scanner.nextLine().trim();
            String[] inputs = input.split(" ");

            if(inputs.length == 0 || inputs[0].isEmpty()) { continue; }

            String command = inputs[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "logout" -> {
                        server.logout(auth.authToken());
                        System.out.println("Logged out");
                        running = false;
                    }
                    case "quit" -> {
                        return;
                    }
                    case "create" -> handleCreate(inputs);
                    case "list" -> doListGames();
                    case "join" -> handleJoin(inputs);
                    case "observe" -> handleObserve(inputs);
                    default -> {
                        System.out.println("Invalid command: please try again");
                        printHelp();
                    }
                }
            }catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void handleCreate(String[] inputs) {
        if (inputs.length != 2) {
            System.out.println("USE: create <NAME>");
            return;
        }
        doCreateGame(inputs[1]);
    }

    private void handleJoin(String[] inputs) {
        if(inputs.length != 3) {
            System.out.println("USE: join <ID> [WHITE|BLACK]");
            return;
        }

        int index = Integer.parseInt(inputs[1]) - 1;
        if(index < 0 || index >= gameLists.size()) {
            System.out.println("Invalid game number");
            return;
        }

        GameData gameData = gameLists.get(index);
        String color = inputs[2].toUpperCase();
        ChessGame.TeamColor playerColor = color.equals("BLACK") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        server.joinGame(gameData.gameID(), color, auth.authToken());
        System.out.printf("Joined game %d as %s%n", index+1, color);

        printBoard(gameData, playerColor);
    }

    private void handleObserve(String[] inputs) {
        if(inputs.length != 2) {
            System.out.println("USE: observe <ID>");
            return;
        }

        int index = Integer.parseInt(inputs[1]) - 1;
        if(index < 0 || index >= gameLists.size()) {
            System.out.println("Invalid game number");
            return;
        }

        GameData gameData = gameLists.get(index);

        server.joinObserver(gameData.gameID());
        System.out.printf("Observing game %d%n", index+1);

        printBoard(gameData);
    }

    private void printHelp() {
        out.println("create <NAME> -> to create a new game");
        out.println("list -> to list existing games");
        out.println("join <ID> [WHITE|BLACK] -> to join a game");
        out.println("observe <ID> -> to observe a game");
        out.println("logout -> to do when you are done");
        out.println("quit -> to stop playing");
        out.println("help -> to show possible commands");
    }

    private void doCreateGame(String name) {
        try {
            server.createGame(name, auth.authToken());
            System.out.println("Created game is " + name);
        }catch (Exception ex) {
            System.out.println("Create failed. Please try again.");
        }
    }

    private void doListGames() {
        try {
            gameLists = server.listGames(auth.authToken());
            if (gameLists != null && !gameLists.isEmpty()) {
                System.out.println("Existing games: ");
                int index = 1;
                for (GameData game : gameLists) {
                    String white = game.whiteUsername() != null ? game.whiteUsername() : "None";
                    String black = game.blackUsername() != null ? game.blackUsername() : "None";
                    System.out.println(index + ": " + game.gameName() + " | white user: " + white + " | black user: " + black);
                    index++;
                }
            } else {
                //when there is no game
                System.out.println("No games");
            }
        }catch(Exception ex) {
            System.out.println("List game failed. Please try again.");
        }
    }

    private void doJoinGame(String stringNum, String color) {
        try {
            int num = Integer.parseInt(stringNum) - 1;
            if(num < 0 || num >= gameLists.size()) {
                System.out.println("Invalid game number");
                return;
            }

            String teamColor = color.toUpperCase();
            if(!teamColor.equals("WHITE") && !teamColor.equals("BLACK")) {
                System.out.println("Please choose a valid color: WHITE or BLACK");
                return;
            }

            int gameID = gameLists.get(num).gameID();
            server.joinGame(gameID, teamColor, auth.authToken());
            System.out.println("Joined game: " + (num + 1) + " as " + teamColor);
        }catch (Exception ex) {
            System.out.println("Invalid process. Please try again.");
        }
    }

    private void printBoard(GameData gameData, ChessGame.TeamColor color) {
        ChessGame game = new ChessGame();
        GameplayRepl gameUI = new GameplayRepl(server, gameData, game, color);
        gameUI.run();
    }

    private void printBoard(GameData gameData) {
        ChessGame game = new ChessGame();
        GameplayRepl gameUI = new GameplayRepl(server, gameData, game);
        gameUI.run();
    }

}
