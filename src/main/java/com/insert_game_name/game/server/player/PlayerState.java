package com.insert_game_name.game.server.player;

public final record PlayerState(
    String nickname,
    boolean connected
) {}