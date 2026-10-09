/*
 * Copyright (C) 2026 Leonardo Ricci Mingani (NeekoKun)
 *
 * This file is part of IdS-proj.
 *
 * IdS-proj is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IdS-proj is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IdS-proj. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.insert_game_name.game.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert_game_name.game.client.events.*;
import com.insert_game_name.game.server.events.*;
import com.insert_game_name.game.server.lobby.LobbyState;
import com.insert_game_name.game.server.GameState;

import java.util.regex.Matcher;

/** Client-side connection and command-line interface for the game server. */
public class Client {
    private final String username;
    private final Socket serverSocket;
    private final Scanner scanner;
    private final BlockingQueue<ClientEvent> outbox = new LinkedBlockingQueue<ClientEvent>();
    private final BlockingQueue<ServerEvent> inbox  = new LinkedBlockingQueue<ServerEvent>();
    private List<LobbyState> lobbyHistory = new ArrayList<LobbyState>(20); //Never call outside of addLobbyState()
    private ObjectMapper MAPPER = new ObjectMapper();
    private Thread inputThread;
    private Thread readerThread;
    private Thread writerThread;
    private LobbyState lobby;
    private GameState game;

    /**
     * Creates a client and initializes its lobby data and input mode.
     *
     * @param user the authenticated username
     * @param connection the socket connected to the server
     * @param scan the source of client input
     * @param mode the input mode to use
     */
    public Client(String user, Socket connection, Scanner scan, String mode) {
        username = user;
        serverSocket = connection;
        scanner = scan;
        Thread input;
        readerThread = new Thread(() -> Reader());
        writerThread = new Thread(() -> Writer());

        
        switch (mode) {
            case "CLI":
                input = new Thread(() -> readCli());
                break;
                
            default: //Implement TUI and GUI
                return;
        }

            inputThread = input;
            readerThread.start();
            writerThread.start();
            inputThread.start();

        try {
            handleEvents();
        } catch (InterruptedException e) {
            System.out.println("User interrupt");
            return;
        }
    }

    /** Processes server events received by the client. */
    private void handleEvents() throws InterruptedException {
        ServerEvent event;
        while (true) {
            event = inbox.take();
            switch (event) {
                case ServerError _ -> {
                    System.out.println("[+] Received error response from server");
                }
                case ServerForcedDisconnect _ -> {
                    System.out.println("[+] Received forced disconnect from server");
                    shutdown();
                    System.exit(0);
                    return;
                }
                case ServerHeartbeat _ -> {
                    System.out.println("[+] Received heartbeat from server");
                }
                case ServerMessage _ -> {
                    System.out.println("[+] Received new chat message");
                }
                case ServerNotification notification -> {
                    System.out.println("[+] Received new lobby state from server");
                    addLobbyState(notification.lobby());
                }
                case ServerOk _ -> {
                    System.out.println("[+] Received Ok");
                }
                case ServerUpdate _ -> {
                    System.out.println("[+] Received new game state from server");
                }
            }
        }
    }

    /** Stops client workers and closes the server connection. */
    private void shutdown() {
        if (inputThread != null) inputThread.interrupt();
        if (readerThread != null) readerThread.interrupt();
        if (writerThread != null) writerThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // The connection is already closed.
        }
    }

    /**
     * Adds a lobby state to the front of the history, retaining at most 20 entries.
     *
     * @param lobbyState the lobby state to add
     * @return {@code true} if the new lobbyState was implemented in the history
     */
    public boolean addLobbyState(LobbyState lobbyState){
        try {
            if (lobbyState.version() < lobbyHistory.getLast().version()) return false;
        } catch (NoSuchElementException e) {}

        if (lobbyHistory.size() >= 20) {
            lobbyHistory.removeLast();
            lobbyHistory.addFirst(lobbyState);
            return true;
        } else {
            lobbyHistory.addFirst(lobbyState);
            return true;
        }
    }

    /** Prints the most recently stored lobby state. */
    public void printLatestLobby() throws IllegalStateException {
        if (lobbyHistory.isEmpty()) {
            throw new IllegalStateException("No lobby history available");
        }

        System.out.println(lobbyHistory.get(0));
    }

    private void Writer() {
        ClientEvent outputEvent;
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream(), StandardCharsets.UTF_8));
            while (true) {
                outputEvent = outbox.take();
                writer.write(MAPPER.writeValueAsString(outputEvent));
                writer.write('\n');
                writer.flush();
            }
            // Yada Yada connection closed
        } catch (InterruptedException e) {
            return;
        } catch (IOException e) {
            System.out.println("Connection dropped.");
        }
    }

    private void Reader() {
        BufferedReader reader;
        String line;
        ServerEvent event;
        try {
            reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), StandardCharsets.UTF_8));
            
            while (true) { // Actual socket reading logic
                line = reader.readLine();

                if (line == null) {
                    break;
                }

                try {
                    event = MAPPER.readValue(line, ServerEvent.class);
                } catch (JsonProcessingException e) {
                    continue;
                }

                inbox.offer(event);
            }
        } catch (IOException e) {
            System.out.println("Error reading the data from server");
            return;
        }
    }

    /** Reads and dispatches commands entered through the command-line interface.
     * Should never directly handle the network. Instead, it can push events to an outbox
     * 
     */
    /**
     * Reads commands from the console and queues corresponding client events.
     */
    public void readCli() {
        String[] args;
        while (true) {
            args = this.scanner.nextLine().split(" ");

            if(args.length == 0) continue;

            switch (args[0]) {
                case "lobby":
                    try {
                        printLatestLobby();
                    } catch (IllegalStateException e) {
                        System.out.println("No lobby data found, fetch from the server.");
                    }
                    break;
                
                case "getLobby":
                    outbox.offer(new ClientRequestLobbyState(0)); //TODO: Randomize ids
                    System.out.println("[-] Sending lobby data request");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Registers a new account using the server connection.
     *
     * @param in server response stream
     * @param out server request stream
     * @param scanner source of registration input
     * @return {@code true} if registration succeeds
     * @throws IOException if communication with the server fails
     */
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
            System.out.println("[warn] Registration unsuccessful");
            return false; //TODO: Better detection of duplicate username
        }
    }

    /**
     * Authenticates a user using the server connection.
     *
     * @param in server response stream
     * @param out server request stream
     * @param scanner source of login input
     * @return the authenticated username, or {@code null} if authentication fails
     * @throws IOException if communication with the server fails
     */
    public static String login(BufferedReader in, BufferedWriter out, Scanner scanner) throws IOException {
        System.out.println("Login form");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        out.write(String.format("register=false&username=%s&password=%s", username, password));
        out.newLine();
        out.flush();

        String response = in.readLine();

        if (response.equals("OK")) {
            System.out.println("[info] Login succesful");
            return username;
        } else  if (response.equals("DISCONNECTING")) {
            System.out.println("[info] login successful, waiting for other user's disconnect");
            response = in.readLine();
            if (response.equals("OK")) {
                System.out.println("[info] login successful");
                return username;
            } else {
                System.out.println("[warn] something went wrong");
                return null;
            }
        } else {
            System.out.println("[warn] Login unsuccessful");
            return null;
        }
    }

    /**
     * Connects to the server, performs authentication, and starts the client.
     *
     * @param args command-line arguments (currently unused)
     */
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
                    username = login(in, out, scanner);
                    
                    if (username == null) {
                        System.out.println("Couldn't log in with the provided credentials");
                        continue;
                    }
                    
                    break;
                } else {
                    continue;
                }
            }

            Client c = new Client(username, socket, scanner, "CLI");

        } catch (IOException e) {
            System.out.println("Connection to " + serverIp + ":" + serverPort + " closed abruptly.");
        }

    }
}
