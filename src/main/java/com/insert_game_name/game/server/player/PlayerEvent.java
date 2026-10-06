package com.insert_game_name.game.server.player;

import com.insert_game_name.game.client.events.ClientEvent;

/** Associates a decoded client event with the player that sent it. */
public record PlayerEvent (
    Player player,
    ClientEvent event
) {}
