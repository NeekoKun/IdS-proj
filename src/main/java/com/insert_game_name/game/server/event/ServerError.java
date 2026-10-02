package com.insert_game_name.game.server.event;

public record ServerError(
    int id,
    int type,
    String message
) implements ServerEvent {}