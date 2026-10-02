package com.insert_game_name.game.server.character;

public class GameCharacter {
    public GameCharacterPublicState publicState() {
        return new GameCharacterPublicState();
    } 

    public GameCharacterPrivateState privateState() {
        return new GameCharacterPrivateState();
    }
}
