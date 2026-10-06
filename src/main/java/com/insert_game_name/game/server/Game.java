package com.insert_game_name.game.server;

import java.util.ArrayList;
import java.util.List;

import com.insert_game_name.game.server.character.*;

/** Holds the mutable score and characters for an active game. */
public class Game {
    private int score;
    private ArrayList<GameCharacter> characters;

    /**
     * Creates a game with a starting score and a copy of its characters.
     *
     * @param starting_score initial game score
     * @param players characters participating in the game
     */
    public Game(int starting_score, ArrayList<GameCharacter> players) {
        score = starting_score;

        characters = new ArrayList<GameCharacter>(players);
    }

    /**
     * Builds the public state currently visible to clients.
     *
     * @return a snapshot containing the score and public character states
     */
    public GameState state() {
        List<GameCharacterPublicState> character_states = new ArrayList<GameCharacterPublicState>();
        
        for (GameCharacter character : characters) {
            character_states.add(character.publicState());
        }
        
        GameState state = new GameState(score, character_states);

        return state;
    }

    /** @return the current game score */
    public int getScore() {
        return score;
    }

    /**
     * Decreases the score by the supplied amount.
     *
     * @param delta amount to subtract from the score
     */
    public void lowerScore(int delta) {
        score -= delta;
    }
}
