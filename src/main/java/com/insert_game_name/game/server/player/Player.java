package com.insert_game_name.game.server.player;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert_game_name.game.client.Client;
import com.insert_game_name.game.client.events.ClientEvent;
import com.insert_game_name.game.server.event.*;


public class Player {
    public static int count = 0;
    public int id;
    public Socket socket;
    public String username;
    private DataOutputStream out;
    private LinkedBlockingQueue<ServerEvent> outbox;
    private ObjectMapper MAPPER;
    private volatile boolean stopped;
    private Future<?> readerTask;
    private Future<?> writerTask;

    public Player(Socket client_socket, ObjectMapper mapper) throws IOException {
        id = count++;
        socket = client_socket;
        out = new DataOutputStream(client_socket.getOutputStream());
        outbox = new LinkedBlockingQueue<ServerEvent>();
        MAPPER = mapper;
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

    private void Reader(LinkedBlockingQueue<ClientEvent> inbox) {
        BufferedReader reader;
        String line;
        ClientEvent event;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            
            while (!stopped) { // Actual socket reading logic
                line = reader.readLine();

                if (line == null) {
                    break;
                }

                try {
                    event = MAPPER.readValue(line, ClientEvent.class);
                } catch (JsonProcessingException e) {
                    continue;
                }

            }
        } catch (IOException e) {
            System.out.println("Error reading the data socket for player id ["+id+"]");
            return;
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
        while (!stopped) {
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

    public void start(ExecutorService pool, LinkedBlockingQueue<ClientEvent> inbox) {
        readerTask = pool.submit(() -> Reader(inbox));
        writerTask = pool.submit(() -> Writer());
    }

    public void stop() {
        if (stopped) {
            return;
        }

        stopped = true; //TODO: Send a ServerForcedDisconnect before shutting down the connection
        outbox.clear();

        if (readerTask != null) {
            readerTask.cancel(true);
        }
        if (writerTask != null) {
            writerTask.cancel(true);
        }

        try {
            socket.close();
        } catch (IOException e) {
            // The socket may already be closed.
        }
    }

    public PlayerState state() {
        return new PlayerState("lol", true);
    }
}
