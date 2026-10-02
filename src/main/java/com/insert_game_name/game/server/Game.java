package com.insert_game_name.game.server;

import java.util.ArrayList;
import java.util.List;

import com.insert_game_name.game.server.character.*;

public class Game {
    private int score;
    private ArrayList<GameCharacter> characters;

    public Game(int starting_score, ArrayList<GameCharacter> players) {
        score = starting_score;

        characters = new ArrayList<GameCharacter>(players);
    }

    public GameState state() {
        List<GameCharacterPublicState> character_states = new ArrayList<GameCharacterPublicState>();
        
        for (GameCharacter character : characters) {
            character_states.add(character.publicState());
        }
        
        GameState state = new GameState(score, character_states);

        return state;
    }

    public int getScore() {
        return score;
    }

    public void lowerScore(int delta) {
        score -= delta;
    }
}
