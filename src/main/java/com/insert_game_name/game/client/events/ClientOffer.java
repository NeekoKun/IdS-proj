package com.insert_game_name.game.client.events;

/** Application-defined offer (game update) sent by a client during game play. */
public record ClientOffer(
    int id,
    int type,
    String data
) implements ClientEvent {}
