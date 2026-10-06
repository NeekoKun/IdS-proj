package com.insert_game_name.game.client.events;

/** Application-defined notification (lobby update) sent by a client. */
public record ClientNotificationOffer(
    int id,
    int type,
    String data
) implements ClientEvent {}
