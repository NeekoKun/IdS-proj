package com.insert_game_name.game.client.events;

/** Client liveness event containing the request identifier. */
public record ClientHeartbeat(
    int id
) implements ClientEvent {}