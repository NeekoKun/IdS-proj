package com.insert_game_name.game.client.events;

public record ClientHello(
    int id,
    String username
) implements ClientEvent {}
