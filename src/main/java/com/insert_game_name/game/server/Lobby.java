package com.insert_game_name.game.server;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.LinkedList;

import com.insert_game_name.game.client.events.ClientEvent;
import com.insert_game_name.game.server.player.Player;
import com.insert_game_name.game.server.player.PlayerState;

public class Lobby implements Runnable {
    private static int count = 0;
    public int id;
    private List<Player> players = new LinkedList<Player>();
    public int gameState; // "open", "starting", "playing", "closing", "finished"
    private ExecutorService playerPool;
    private LinkedBlockingQueue<ClientEvent> inbox;

    public Lobby(ExecutorService pool) {
        id = count++;
        gameState = 0;
        playerPool = pool;
        inbox = new LinkedBlockingQueue<ClientEvent>();
    }

    @Override 
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ClientEvent event = inbox.take();
                //Handle Event
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.start(playerPool, inbox);
    }

    public void removePlayer(int id) {
        int index = 0;
        for (Player player : players) {

            if (player.id == id) {
                player.stop();
                players.remove(index);
            }

            index++;
        }
    }

    public LobbyState state() {
        List<PlayerState> player_states = new ArrayList<PlayerState>();
        
        for (Player player : players) {
            player_states.add(player.state());
        }
        
        LobbyState state = new LobbyState(gameState, player_states);

        return state;
    }
}
