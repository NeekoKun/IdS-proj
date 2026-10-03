package com.insert_game_name.game.server.event;

import com.insert_game_name.game.server.LobbyState;

public record ServerNotification(
    int counter, 
    int type,
    LobbyState lobby
) implements ServerEvent {}