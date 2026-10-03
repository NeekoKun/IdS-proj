package com.insert_game_name.game.client.events;

public record ClientNotificationOffer(
    int id,
    int type,
    String data
) implements ClientEvent {}
