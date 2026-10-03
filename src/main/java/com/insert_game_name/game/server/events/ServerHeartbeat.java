package com.insert_game_name.game.server.events;

public record ServerHeartbeat(
    int ttl
) implements ServerEvent {}