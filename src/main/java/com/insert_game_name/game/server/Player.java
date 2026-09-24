package com.insert_game_name.game.server;

import java.net.Socket;

public class Player {
    public static int count = 0;
    public int id;
    public Socket socket;

    public Player(Socket client_socket) {
        id = count++;
        socket = client_socket;
    }
}
