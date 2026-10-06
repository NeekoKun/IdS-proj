package com.insert_game_name.game.server.player;

/** Immutable snapshot of a player's state */
public final record PlayerState(
    String nickname
) {}