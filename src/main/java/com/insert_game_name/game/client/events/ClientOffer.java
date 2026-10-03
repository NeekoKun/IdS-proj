package com.insert_game_name.game.client.events;

public record ClientOffer(
    int id,
    int type,
    String data
) implements ClientEvent {}
