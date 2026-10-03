package com.insert_game_name.game.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert_game_name.game.server.player.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

    public static void main(String[] args) {
        // TODO: Load from file

        // Listen to clients
        try (ServerSocket serverSocket = new ServerSocket(4321)) {
            serverSocket.setReuseAddress(true);

            while (!serverSocket.isClosed()) {
                Socket connection = serverSocket.accept();
                Thread.startVirtualThread(() -> handleConnection(connection));
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

    private static void handleConnection(Socket connection) {
        try {
            Player player = new Player(connection, MAPPER);
            Integer lobbyId = findOrCreateLobby();

            lobbies.get(lobbyId).addPlayer(player);
        } catch (IOException exception) {
            System.out.println("Error handling a connection to player");
        }
    }
}
