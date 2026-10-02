package com.insert_game_name.game.server.event;

import org.json.JSONObject;

public record ServerNotification(
    int counter, 
    int type,
    JSONObject lobby
) implements ServerEvent {}