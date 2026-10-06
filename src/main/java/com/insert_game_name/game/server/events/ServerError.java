package com.insert_game_name.game.server.events;

/** Reports an error while processing a client request. */
public record ServerError(
    int id,
    int type,
    String message
) implements ServerEvent {}