package com.insert_game_name.game.server.player;

import com.insert_game_name.game.client.events.ClientEvent;

public record PlayerEvent (
    Player player,
    ClientEvent event
) {}
