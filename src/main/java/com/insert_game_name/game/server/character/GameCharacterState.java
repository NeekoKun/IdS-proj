package com.insert_game_name.game.server.character;

/** Common marker for public and private character state snapshots. */
public sealed interface GameCharacterState
    permits
        GameCharacterPrivateState,
        GameCharacterPublicState
    {}
