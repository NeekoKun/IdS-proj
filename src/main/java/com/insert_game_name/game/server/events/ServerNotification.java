package com.insert_game_name.game.server.events;

import com.insert_game_name.game.server.LobbyState;

/** Delivers a Lobby State to a client.*/
public record ServerNotification(
    int counter, 
    Integer type,
    LobbyState lobby
) implements ServerEvent {}