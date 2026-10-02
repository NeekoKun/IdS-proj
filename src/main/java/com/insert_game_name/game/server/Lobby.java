package com.insert_game_name.game.server;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import org.json.JSONObject;

import com.insert_game_name.game.server.player.Player;
import com.insert_game_name.game.server.player.PlayerState;

public class Lobby {
    private static int count = 0;
    public int id;
    private List<Player> players = new LinkedList<Player>();
    private int game_state; // "open", "starting", "playing", "closing", "finished"

    public Lobby() {
        id = count++;
        game_state = "open";
    }

    public void addPlayer(Player player) {
        players.add(player);
        sendLobbyUpdate();
    }

    public void removePlayer(int id) {
        int index = 0;
        for (Player player : players) {

            if (player.id == id) {
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
        
        LobbyState state = new LobbyState(game_state, player_states);

        return state;
    }
}
