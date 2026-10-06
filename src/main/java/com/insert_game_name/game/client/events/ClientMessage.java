package com.insert_game_name.game.client.events;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Chat message sent by a client, either publicly or to one recipient. */
public record ClientMessage(
    int id,
    @JsonProperty("private") boolean isPrivate,
    String recipient,
    String content
) implements ClientEvent {}