package com.insert_game_name.game.server.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ServerMessage(
    int counter,
    @JsonProperty("private") boolean isPrivate,
    long timestamp,
    String author,
    String content
) implements ServerEvent {}
