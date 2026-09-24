package com.insert_game_name.game.server;

import java.util.ArrayList;
import java.util.List;
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
	private static List<Lobby> lobbies = new ArrayList<Lobby>();

    public static void main(String[] args) {
        // TODO: Load from file

        // Listen to clients
        try (ServerSocket server_socket = new ServerSocket(4321)) {
            server_socket.setReuseAddress(true);

            while (true) {
                Socket connection = server_socket.accept();

                handle_connection(connection);
            }

        } catch (IOException e) {
            System.err.println(e);
        }
	}

    public static void handle_connection(Socket socket) {
        boolean free = false;
        int index = -1;

        // 1. Create a new Player instance

        Player player = new Player(socket);

        // 2. Choose a lobby or create a new one

        for (Lobby lobby : lobbies) {
            if (lobby.getGameState().equals("open")) {
                free = true;
                index = lobbies.indexOf(lobby);
                break;
            }
        }

        if (!free) {
            lobbies.add(new Lobby());
            index = lobbies.size() - 1;
        }

        lobbies.get(index).addPlayer(player);

        // 3. Send lobby informations to the client

        
    }
}
