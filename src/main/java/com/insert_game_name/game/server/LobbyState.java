package com.insert_game_name.game.server;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.insert_game_name.game.server.player.PlayerState;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
/** Immutable snapshot of a lobby and its players. */
public record LobbyState (
    int version,
    int lobbyPhase,
    List<PlayerState> playerState
) {}