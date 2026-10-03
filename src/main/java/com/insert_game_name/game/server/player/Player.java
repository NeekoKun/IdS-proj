package com.insert_game_name.game.server.player;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert_game_name.game.client.events.ClientEvent;
import com.insert_game_name.game.server.events.*;


public class Player {
    // Player data
    public static int count = 0;
    public int id;
    public Socket socket;
    public String username;
    public long latestSignal;
    public boolean lobbyAdmin;

    // Components
    private LinkedBlockingQueue<ServerEvent> outbox;
    private ObjectMapper MAPPER;
    private volatile boolean stopped;
    private Future<?> readerTask;
    private Future<?> writerTask;

    public Player(Socket client_socket, ObjectMapper mapper) throws IOException {
        id = count++;
        socket = client_socket;
        outbox = new LinkedBlockingQueue<ServerEvent>();
        MAPPER = mapper;
        latestSignal = System.currentTimeMillis();
    }

    public void send(ServerEvent event) {
        outbox.offer(event);
    }

    private void Reader(LinkedBlockingQueue<PlayerEvent> inbox) {
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

                push(new PlayerEvent(this, event), inbox);
            }
        } catch (IOException e) {
            System.out.println("Error reading the data socket for player id ["+id+"]");
            return;
        }
    }

    private void push(PlayerEvent playerEvent, LinkedBlockingQueue<PlayerEvent> inbox) {
        boolean accepted;
        do {
            accepted = inbox.offer(playerEvent);
        } while (!accepted);
        return;
    }

    private void Writer() {
        ServerEvent outputEvent;
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            while (!stopped) {
                outputEvent = outbox.take();
                writer.write(MAPPER.writeValueAsString(outputEvent));
                writer.write('\n');
                writer.flush();
            }
            // Yada Yada connection closed
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (IOException e) {
            if (!stopped) {
                System.out.println("Error writing for player id ["+id+"]");
            }
        }
    }

    public void start(ExecutorService pool, LinkedBlockingQueue<PlayerEvent> inbox) {
        readerTask = pool.submit(() -> Reader(inbox));
        writerTask = pool.submit(() -> Writer());
    }

    public void stop() {
        if (stopped) {
            return;
        }

        stopped = true;
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
