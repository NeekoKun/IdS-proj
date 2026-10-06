package com.insert_game_name.game.client.events;

/** Requests the current lobby state from the server. */
public record ClientRequestLobbyState(
    int id
) implements ClientEvent {}
