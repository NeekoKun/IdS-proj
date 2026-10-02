package com.insert_game_name.game.server;

import java.util.List;

import com.insert_game_name.game.server.character.GameCharacterPublicState;

public record GameState (
    int score,
    List<GameCharacterPublicState> characterStates 
) {}
