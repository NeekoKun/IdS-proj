package com.insert_game_name.game.server.events;

/** Server liveness event containing the client timeout value. */
public record ServerHeartbeat(
    int ttl
) implements ServerEvent {}