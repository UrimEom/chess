package ui;

import model.AuthData;

import java.util.Scanner;

import static java.lang.System.out;

public class PreloginRepl {
    private final ServerFacade server;
    private final Scanner scanner;

    public PreloginRepl(ServerFacade server) {
        this.server = server;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        out.println("Welcome to 240 chess! Type Help to get started.");

        while(true) {
            out.print("\n[LOGGED_OUT] >>> ");
            String input = scanner.nextLine().trim();
            String[] inputs = input.split(" ");

            if(inputs.length == 0 || inputs[0].isEmpty()) { continue; }

            String command = inputs[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "quit" -> {
                        return;
                    }
                    case "login" -> {
                        if (inputs.length != 3) {
                            System.out.println("USE: login <USERNAME> <PASSWORD>");
                        } else {
                            doLogin(inputs[1], inputs[2]);
                        }
                    }
                    case "register" -> {
                        if (inputs.length != 4) {
                            System.out.println("USE: register <USERNAME> <PASSWORD> <EMAIL>");
                        } else {
                            doRegister(inputs[1], inputs[2], inputs[3]);
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
        out.println("register <USERNAME> <PASSWORD> <EMAIL> -> to create an account");
        out.println("login <USERNAME> <PASSWORD> -> to play chess");
        out.println("quit -> to stop playing chess");
        out.println("help -> to show possible commands");
    }

    private void doLogin(String username, String password) {
        AuthData auth = server.login(username, password);
        System.out.println("Logged in as " + auth.username());
        new PostloginRepl(server, auth).run();
    }

    private void doRegister(String username, String password, String email) {
        AuthData auth = server.register(username, password, email);
        System.out.println("Registered and logged in as " + auth.username());
        new PostloginRepl(server,auth).run();
    }
}
