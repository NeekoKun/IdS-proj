package com.insert_game_name.game.server.character;

public sealed interface GameCharacterState
    permits
        GameCharacterPrivateState,
        GameCharacterPublicState
    {}
