package com.insert_game_name.game.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.HashMap;

import com.insert_game_name.game.client.events.*;
import com.insert_game_name.game.server.events.ServerForcedDisconnect;
import com.insert_game_name.game.server.player.*;

public class Lobby implements Runnable {
    private static int count = 0;
    public int id;
    private final Map<String, Player> players = new HashMap<String, Player>();
    public int gameState; // "open", "starting", "playing", "closing", "finished"
    private ExecutorService playerPool;
    private LinkedBlockingQueue<PlayerEvent> inbox;

    public Lobby(ExecutorService pool) {
        id = count++;
        gameState = 0;
        playerPool = pool;
        inbox = new LinkedBlockingQueue<PlayerEvent>();
    }

    @Override 
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                PlayerEvent playerEvent = inbox.take();
                ClientEvent event = playerEvent.event();
                Player player = playerEvent.player();
                switch (event) {
                    case ClientHeartbeat clientHeartbeatEvent -> {
                        player.latestSignal = System.currentTimeMillis();
                    }
                    case ClientDisconnectAlert clientDisconnectAlert -> {
                        player.send(new ServerForcedDisconnect(ServerForcedDisconnect.ACCEPTED_DISCONNECT, null));
                    }
                    case ClientNotificationOffer clientNotificationOffer -> {
                        if (!player.lobbyAdmin) break;
                        //TODO: process notification
                        player.latestSignal = System.currentTimeMillis();
                    }
                    case ClientOffer clientOffer -> {
                        //TODO: pass offer to game
                        player.latestSignal = System.currentTimeMillis();
                    }
                    case ClientMessage clientMessage -> {
                        //TODO: Send chat message
                        player.latestSignal = System.currentTimeMillis();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void addPlayer(Player player) {
        players.put(player.username, player);
        if (players.size() == 1) {
            player.lobbyAdmin = true;
        }
        player.start(playerPool, inbox);
    }

    public void removePlayer(String username) {
        //TODO: Send a ServerForcedDisconnect event
        players.get(username).stop();
        players.remove(username);
    }

    public LobbyState state() {
        List<PlayerState> playerStates = new ArrayList<PlayerState>(players.size());
        
        for (Player player : players.values()) {
            playerStates.add(player.state());
        }
        
        LobbyState state = new LobbyState(gameState, playerStates);

        return state;
    }
}
