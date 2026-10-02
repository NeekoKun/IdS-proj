package com.insert_game_name.game.server.player;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.insert_game_name.game.server.event.*;


public class Player {
    public static int count = 0;
    public int id;
    public Socket socket;
    public String username;
    private DataOutputStream out;
    private DataInputStream in;
    private LinkedBlockingQueue<ServerEvent> outbox;

    public Player(Socket client_socket) throws IOException {
        id = count++;
        socket = client_socket;
        in = new DataInputStream(client_socket.getInputStream());
        out = new DataOutputStream(client_socket.getOutputStream());
        outbox = new LinkedBlockingQueue<ServerEvent>();
    }

    public void send(String data) {
        try {
            byte[] output = data.getBytes();
            
            out.write(output.length);
            out.write(output);

        } catch (IOException e) {
            System.out.print(e);
        }
    }

    private Runnable Reader(LinkedBlockingQueue<ServerEvent> inbox) {
        JSONObject data;
        while (true) {
            try {
                int len = in.readInt();
                if (len < 0 || len > 1_000_000) continue;
                byte[] buf = new byte[len];
                in.readFully(buf);

                data = new JSONObject(new String(buf, StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                Disconnected event = new Disconnected(this);
                push(event, inbox);
                continue;
            }
            // Process the read buffer
            try {
                if (data.get("type").equals("message")) {
                    //do shit
                }
            } catch (JSONException exception) {
                //Malformed data received
            }
        }
    }

    private void push(ServerEvent event, LinkedBlockingQueue<ServerEvent> inbox) {
        boolean accepted;
        do {
            accepted = inbox.offer(event);
        } while (!accepted);
        return;
    }

    private void Writer() {
        ServerEvent output_event;
        while (true) {
            try { output_event = outbox.take(); } catch (InterruptedException e) { break; }
            switch (output_event) {
                case ServerMessage message -> {

                }
                case ServerForcedDisconnect disconnect -> {

                }
                default -> {

                }
            }
        }
        // Yada Yada connection closed
    }

    public void start(ExecutorService pool, LinkedBlockingQueue<ServerEvent> inbox) {
        pool.submit(Reader(inbox));
        pool.submit(() -> Writer());
    }

    public PlayerState state() {
        return new PlayerState("lol", true);
    }
}
