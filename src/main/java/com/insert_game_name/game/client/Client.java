package com.insert_game_name.game.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Client {
    private final String username;
    private final String token;
    private final Socket serverSocket;
    private final Scanner scanner;

    public Client (String user, String tok, Socket connection, Scanner scan) {
        username = user;
        token = tok;
        serverSocket = connection;
        scanner = scan;
    }

    public static boolean register(BufferedReader in, BufferedWriter out, Scanner scanner) throws IOException {
        System.out.println("Registration form");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Repeated the passwird: ");
        String passwordCheck = scanner.nextLine();

        if (!password.equals(passwordCheck)) {
            System.out.println("The passwords don't match");
            return false;
        }

        out.write(String.format("register=true&username=%s&password=%s", username, password));
        out.newLine();
        out.flush();

        if (in.readLine().equals("OK")) {
            System.out.println("[info] Registration successful");
            return true;
        } else {
            System.out.println("[info] Registration unsuccessful");
            return false; //TODO: Better detection of duplicate username
        }
    }

    public static String login(BufferedReader in, BufferedWriter out, Scanner scanner) throws IOException {
        System.out.println("Login form");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        out.write(String.format("register=false&username=%s&password=%s", username, password));
        out.newLine();
        out.flush();

        if (in.readLine().equals("OK")) {
            System.out.println("[info] Login succesful");
            return username + ":" + in.readLine();
        } else {
            System.out.println("[warn] login unsuccesful");
            return null;
        }
    }

    public static void main(String[] args) {
        String serverIp;
        int serverPort;
        Scanner scanner = new Scanner(System.in);
        Pattern ipPattern = Pattern.compile("^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher matcher;
        
        //do {
        //    System.out.print("input server ip> ");
        //    serverIp = scanner.nextLine();
        //    matcher = ipPattern.matcher(serverIp);
        //} while (!matcher.matches());
        //
        //while(true) {
        //    System.out.print("input server port> ");
        //    try {
        //        serverPort = Integer.parseInt(scanner.nextLine());
        //        if (0 < serverPort && serverPort < 65535) break;
        //    } catch (NumberFormatException ignore) {}
        //    System.out.println("Invalid int");
        //}

        serverIp = "127.0.0.1";
        serverPort = 4321;
        System.out.println("Trying to enstablish connection with "+serverIp+":"+serverPort+"...");

        try (Socket socket = new Socket(serverIp, serverPort)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); 
            String token;
            String input;
            String username;

            System.out.println("[info] Connection enstablished.");
            
            // Auth cycle
            while (true) {
                System.out.print("Do you wish to register a new account? (y/n): ");
                input = scanner.nextLine().toLowerCase();
                
                if (input.equals("y")) {
                    register(in, out, scanner);
                } else if (input.equals("n")) {
                    String usernameToken = login(in, out, scanner);
                    
                    if (usernameToken == null) {
                        System.out.println("Couldn't log in with the provided credentials");
                        continue;
                    }
                    
                    username = usernameToken.split(":")[0];
                    token = usernameToken.split(":")[1];
                    break;
                } else {
                    continue;
                }
            }

            Client c = new Client(username, token, socket, scanner);
        } catch (IOException e) {
            System.out.println("Connection to " + serverIp + ":" + serverPort + " closed abruptly.");
        }

    }
}
