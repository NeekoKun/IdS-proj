package com.insert_game_name.game.server.player;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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


/** Represents an authenticated player and manages the player's socket I/O. */
public class Player {
    // Player data
    public static int count = 0;
    public int id;
    public Socket socket;
    public final String username;
    public long latestSignal;
    public boolean lobbyAdmin;
    public boolean connected; // Signal whether the client is capable of interpreting Client/Server Event data

    // Components
    private LinkedBlockingQueue<ServerEvent> outbox;
    private ObjectMapper MAPPER;
    private volatile boolean stopped;
    private Future<?> readerTask;
    private Future<?> writerTask;

    /**
     * Creates a player for an authenticated socket connection.
     *
     * @param client_socket socket connected to the client
     * @param mapper mapper used for event serialization
     * @param user authenticated username
     */
    public Player(Socket client_socket, ObjectMapper mapper, String user) {
        id = count++;
        socket = client_socket;
        outbox = new LinkedBlockingQueue<ServerEvent>();
        MAPPER = mapper;
        latestSignal = System.currentTimeMillis();
        username = user;
    }

    /** Queues an event for transmission to the client. @param event event to send */
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

                pushInbox(new PlayerEvent(this, event), inbox);
                this.latestSignal = System.currentTimeMillis();
            }
        } catch (IOException e) {
            System.out.println("Error reading the data socket for player id ["+id+"]");
            return;
        }
    }

    private void pushInbox(PlayerEvent playerEvent, LinkedBlockingQueue<PlayerEvent> inbox) {
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
            return;
        } catch (IOException e) {
            if (!stopped) {
                System.out.println("Error writing for player id ["+id+"]");
            }
        }
    }

    /**
     * Starts the reader and writer tasks for this player's socket.
     *
     * @param pool executor that runs the tasks
     * @param inbox lobby queue receiving decoded client events
     */
    public void start(ExecutorService pool, LinkedBlockingQueue<PlayerEvent> inbox) {
        readerTask = pool.submit(() -> Reader(inbox));
        writerTask = pool.submit(() -> Writer());
    }

    /** Attempts to disconnect the player gracefully before calling {@code stop()} */
    public void disconnect(int reason, String message) {
        send(new ServerForcedDisconnect(reason, message));
        
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {}
        stop();
    }

    /** Getter for the stopped state */
    public boolean stopped() {
        return stopped;
    }

    /** Stops the I/O tasks, clears pending output, and closes the socket. */
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

    /** @return the public player state sent to lobby clients */
    public PlayerState state() {
        return new PlayerState(username);
    }
}
