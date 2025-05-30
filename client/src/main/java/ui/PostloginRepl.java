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
                    case "create" -> {
                        if (inputs.length != 2) {
                            System.out.println("USE: create <NAME>");
                        } else {
                            doCreateGame(inputs[1]);
                        }
                    }
                    case "list" -> doListGames();
                    case "join" -> {
                        if(inputs.length != 3) {
                            System.out.println("USE: join <ID> [WHITE|BLACK]");
                        }else {
                            doJoinGame(inputs[1], inputs[2]);

                            String color = inputs[2].toUpperCase();
                            ChessGame.TeamColor playerColor = color.equals("BLACK") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                            printBoard(playerColor);
                        }
                    }
                    case "observe" -> {
                        if(inputs.length != 2) {
                            System.out.println("USE: observe <ID>");
                        }else {
                            doObserveGame(inputs[1]);

                            printBoard(null);
                        }
                    }
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
        server.createGame(name, auth.authToken());
        System.out.println("Created game is " + name);
    }

    private void doListGames() {
        gameLists = server.listGames(auth.authToken());

        if(gameLists != null && !gameLists.isEmpty()) {
            System.out.println("Existing games: ");
            for (GameData game : gameLists) {
                String white = game.whiteUsername() != null ? game.whiteUsername() : "None";
                String black = game.blackUsername() != null ? game.blackUsername() : "None";
                System.out.println(game.gameName() + " | " + white + " | " + black);
            }
        }
        //when there is no game
        System.out.println("No games");
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

    private void doObserveGame(String stringNum) {
        try {
            int num = Integer.parseInt(stringNum) - 1;
            if(num < 0 || num >= gameLists.size()) {
                System.out.println("Invalid game number");
                return;
            }

            int gameID = gameLists.get(num).gameID();
            server.observeGame(gameID, auth.authToken());
            System.out.println("Observing game: " + (num + 1));
        }catch (Exception ex) {
            System.out.println("Invalid process. Please try again.");
        }
    }

    private void printBoard(ChessGame.TeamColor color) {
        ChessGame game = new ChessGame();
        GameplayRepl gameUI = new GameplayRepl(game, color);
        gameUI.drawBoard();
    }
}
