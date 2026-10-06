package com.insert_game_name.game.client.events;

/** Requests that the server close the client's lobby connection. */
public record ClientDisconnectAlert(
    int id,
    int reason
) implements ClientEvent {}