package com.insert_game_name.game.server.events;

/** Positive acknowledgement of a client event. */
public record ServerOk(
    int id
) implements ServerEvent {}