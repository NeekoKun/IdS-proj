package com.insert_game_name.game.server.character;

import java.util.LinkedList;
import java.util.List;

/** Runtime representation of a game character. */
public class GameCharacter {
    /** @return the character information safe to expose to other players */
    public GameCharacterPublicState publicState() {
        return new GameCharacterPublicState();
    } 

    /** @return the character information intended only for its owner */
    public GameCharacterPrivateState privateState() {
        return new GameCharacterPrivateState();
    }
}
