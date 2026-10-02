package com.insert_game_name.game.server.event;

public record ServerHeartbeat(
    int ttl
) implements ServerEvent {}