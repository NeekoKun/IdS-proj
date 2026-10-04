package com.insert_game_name.game.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert_game_name.game.server.player.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 
 * Server
 * 1. Read file for saved state
 * 2. Populate/create lobby list
 * 3. Accept connections to either
 *  a. New lobby if no lobby is open
 *  b. First open lobby (which should only be one)
 */

public class Server {
    private static final Map<Integer, Lobby> lobbies = new HashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ExecutorService lobbyPool = Executors.newVirtualThreadPerTaskExecutor();
    private static final ExecutorService playerPool = Executors.newVirtualThreadPerTaskExecutor();
    private static Map<String, Player> loggedPlayers = new HashMap<String, Player>();
    private static Map<String, String> passwords = new HashMap<String, String>();
    private static Path varDir;

    public static void main(String[] args) {
        varDir = Paths.get("/var/lib/insert_game_name");

        if (Files.isDirectory(varDir)) {
            //Get data from here
        } else {
            try {
                Files.createDirectories(varDir);
            } catch (IOException e) {
                System.out.println("Not enough privileges to save data locally");
                return;
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(4321)) {
            serverSocket.setReuseAddress(true);

            while (!serverSocket.isClosed()) {
                Socket connection = serverSocket.accept();
                Thread.startVirtualThread(() -> {
                    try {
                        handleConnection(connection);
                    } catch (IOException e) {
                        System.err.println("Dropped connection");
                        Thread.currentThread().interrupt();
                    }
                });
            }

        } catch (IOException e) {
            System.err.println(e);
        }
	}

    private static Integer findOrCreateLobby() {
        for (Lobby lobby : lobbies.values()) {
            if (lobby.gameState == 0) return lobby.id;
        }
        
        Lobby newLobby = new Lobby(playerPool);
        lobbies.put(newLobby.id, newLobby);
        lobbyPool.submit(newLobby);
        return newLobby.id;
    }

    private static void handleConnection(Socket connection) throws IOException {
        System.out.println("Reveived a connection from " + connection.getInetAddress() + ":" + connection.getPort());
    
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        Authenticator auth = new Authenticator(varDir, MAPPER);

        boolean register;
        boolean result;
        String username;
        String password;
        String token;

        login: while (true) {

            String data = in.readLine();
        
            if (data.chars().filter(ch -> ch == '&').count() != 2 || data.chars().filter(ch -> ch == '=').count() != 3) {
                System.out.println("[warn] Malformed access string from client");
                out.write("MALFORMED");
                out.newLine();
                out.flush();
                return;
            } 
        
            register = data.split("&")[0].split("=")[1].equals("true");
            username = data.split("&")[1].split("=")[1];
            password = data.split("&")[2].split("=")[1];
        
            if (register) {
                result = auth.register(username, password);
        
                if (result) {
                    System.out.println("[info] Registration successful for [" + username + "]");
                    out.write("OK");
                    out.newLine();
                    out.flush();
                } else {
                    System.out.println("[warn] Registration unsuccessful for [" + username + "]");
                    out.write("ERR");
                    out.newLine();
                    out.flush();
                }
            } else {
                token = auth.login(username, password);

                if (token != null) {
                    System.out.println("[info] Login successful for [" + username + "]");
                    out.write("OK");
                    out.newLine();
                    out.flush();
                    break login;
                } else {
                    System.out.println("[warn] Login unsuccessful for [" + username + "]");
                    out.write("ERR");
                    out.newLine();
                    out.flush();
                }
            }
        }

        Player player = new Player(connection, MAPPER, username, token);

        loggedPlayers.put(username, player);
        Integer lobbyId = findOrCreateLobby();

        lobbies.get(lobbyId).addPlayer(player);
    }
}
