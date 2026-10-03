package com.insert_game_name.game.client.events;

public record ClientHeartbeat(
    int id
) implements ClientEvent {}