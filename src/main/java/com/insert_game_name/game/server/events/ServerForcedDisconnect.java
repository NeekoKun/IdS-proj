package com.insert_game_name.game.server.events;

public record ServerForcedDisconnect(
    int reason,
    String message
) implements ServerEvent {
    public static final int ACCEPTED_DISCONNECT = 0;
    public static final int TIMEOUT = 1;
    public static final int KICKED = 2;
    public static final int LOBBY_CLOSED = 3;
    public static final int SERVER_SHUTDOWN = 4;
}