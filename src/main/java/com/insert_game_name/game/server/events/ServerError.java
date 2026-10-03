package com.insert_game_name.game.server.events;

public record ServerError(
    int id,
    int type,
    String message
) implements ServerEvent {}