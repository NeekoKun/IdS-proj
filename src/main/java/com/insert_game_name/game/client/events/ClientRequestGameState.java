package com.insert_game_name.game.client.events;

/** Requests the current game state from the server. */
public record ClientRequestGameState(
    int id
) implements ClientEvent {}
