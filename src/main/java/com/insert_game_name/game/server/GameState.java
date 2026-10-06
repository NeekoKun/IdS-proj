package com.insert_game_name.game.server;

import java.util.List;

import com.insert_game_name.game.server.character.GameCharacterPublicState;

/** Immutable public snapshot of the current game state. */
public record GameState (
    int score,
    List<GameCharacterPublicState> characterStates 
) {}
