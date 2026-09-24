package main.java.com.insert_game_name.game.server;

import java.util.List;
import java.io.Serializable;
import java.util.LinkedList;

public class Lobby implements Serializable {
    private static int count = 0;
    public int id;
    private List<Player> players = new LinkedList<Player>();
    private String game_state; // open, starting, playing, closing, finished

    public Lobby() {
        id = count++;
        game_state = "open";
    }

    public void addPlayer(Player player) {
        players.add(player);
        //sendLobbyUpdate();
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

    public String getGameState() {
        return game_state;
    }
}
