package com.insert_game_name.game.server.events;

import com.insert_game_name.game.server.LobbyState;

public record ServerNotification(
    int counter, 
    Integer type,
    LobbyState lobby
) implements ServerEvent {}