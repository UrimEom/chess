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
                    case "login" -> handleLogin(inputs);
                    case "register" -> handleRegister(inputs);
                    default -> {
                        System.out.println("Invalid command: please try again");
                        printHelp();
                    }
                }
            }catch (Exception ex) {
                System.out.println("Please try again.");
            }
        }
    }

    private void handleLogin(String[] inputs) {
        if (inputs.length != 3) {
            System.out.println("USE: login <USERNAME> <PASSWORD>");
            return;
        }
        doLogin(inputs[1], inputs[2]);
    }

    private void handleRegister(String[] inputs) {
        if (inputs.length != 4) {
            System.out.println("USE: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        doRegister(inputs[1], inputs[2], inputs[3]);
    }

    private void printHelp() {
        out.println("register <USERNAME> <PASSWORD> <EMAIL> -> to create an account");
        out.println("login <USERNAME> <PASSWORD> -> to play chess");
        out.println("quit -> to stop playing chess");
        out.println("help -> to show possible commands");
    }

    private void doLogin(String username, String password) {
        try {
            AuthData auth = server.login(username, password);
            new PostloginRepl(server, auth).run();
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Login failed. Please try again.");
        }
    }

    private void doRegister(String username, String password, String email) {
        try {
            AuthData auth = server.register(username, password, email);
            System.out.println("Registered as " + auth.username());
            new PostloginRepl(server, auth).run();
        }catch(Exception ex) {
            if(ex.getMessage().contains("already")) {
                System.out.println("Already taken. Please try again.");
            }else {
                ex.printStackTrace();
                System.out.println("Register failed. Please try again.");
            }
        }
    }
}
