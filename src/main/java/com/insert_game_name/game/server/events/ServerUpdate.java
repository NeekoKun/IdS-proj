package com.insert_game_name.game.server.events;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.insert_game_name.game.server.GameState;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
/** Delivers a Game State to a client. */
public record ServerUpdate(
    int counter,
    GameState gameState
) implements ServerEvent {}
