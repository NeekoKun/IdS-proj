package com.insert_game_name.game.client.events;

public record ClientDisconnectAlert(
    int id,
    int reason
) implements ClientEvent {}