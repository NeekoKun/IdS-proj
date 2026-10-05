package com.insert_game_name.game.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.HashMap;

import com.insert_game_name.game.client.events.*;
import com.insert_game_name.game.server.events.*;
import com.insert_game_name.game.server.player.*;

public class Lobby implements Runnable {
    private static int count = 0;
    public int id;
    private final Map<String, Player> players = new HashMap<String, Player>();
    public int lobbyPhase; // "open", "starting", "playing", "closing", "finished"
    private ExecutorService playerPool;
    private LinkedBlockingQueue<PlayerEvent> inbox;
    private int version;

    public Lobby(ExecutorService pool) {
        id = count++;
        lobbyPhase = 0;
        version = 0;
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
                    case ClientHello clientHello -> {
                        if (!player.connected) {
                            player.connected = true;
                            player.send(new ServerOk(clientHello.id()));
                        }
                    }
                    case ClientHeartbeat clientHeartbeatEvent -> {
                        System.out.println(String.format("[info] User %s called a ClientHeartbeat", player.username));
                    }
                    case ClientDisconnectAlert clientDisconnectAlert -> {
                        System.out.println(String.format("[info] User %s called a ClientDisconnectAlert", player.username));
                        player.send(new ServerForcedDisconnect(ServerForcedDisconnect.ACCEPTED_DISCONNECT, null));
                    }
                    case ClientNotificationOffer clientNotificationOffer -> {
                        System.out.println(String.format("[info] User %s called a ClientNotificationOffer", player.username));
                        if (!player.lobbyAdmin) break;
                        //TODO: process notification
                    }
                    case ClientOffer clientOffer -> {
                        System.out.println(String.format("[info] User %s called a ClientOffer", player.username));
                        //TODO: pass offer to game
                    }
                    case ClientRequestLobbyState clientRequestLobbyState -> {
                        System.out.println(String.format("[info] User %s called a ClientRequestLobbyState", player.username));
                        player.send(new ServerNotification(this.version, null, this.state()));
                    }
                    case ClientRequestGameState clientRequestGameState -> {   
                        System.out.println(String.format("[info] User %s called a ClientRequestGameState", player.username));
                        player.send(new ServerUpdate(0, null));
                    }
                    case ClientMessage clientMessage -> {
                        System.out.println(String.format("[info] User %s called a ClientMessage with contents [%s]", player.username, clientMessage.content()));
                        //TODO: Send chat message
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
        
        LobbyState state = new LobbyState(lobbyPhase, playerStates);

        return state;
    }
}
