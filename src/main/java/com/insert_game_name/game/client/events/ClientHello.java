package com.insert_game_name.game.client.events;

/** Initial handshake event sent by a client after joining a lobby to signal it is ready to process ServerEvent types */
public record ClientHello(
    int id
) implements ClientEvent {}