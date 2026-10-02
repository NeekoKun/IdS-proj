package com.insert_game_name.game.server.event;

public record ServerForcedDisconnect(
    int reason,
    String message
) implements ServerEvent {}


