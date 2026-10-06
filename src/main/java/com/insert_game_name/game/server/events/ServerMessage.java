package com.insert_game_name.game.server.events;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Public or private chat message delivered to a client. */
public record ServerMessage(
    int counter,
    @JsonProperty("private") boolean isPrivate,
    long timestamp,
    String author,
    String content
) implements ServerEvent {}
