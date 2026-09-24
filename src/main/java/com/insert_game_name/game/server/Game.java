package com.insert_game_name.game.server;

public class Game {
    private int score;

    public Game(int starting_score) {
        score = starting_score;
    }

    public int getScore() {
        return score;
    }

    public void lowerScore(int delta) {
        score -= delta;
    }
}
