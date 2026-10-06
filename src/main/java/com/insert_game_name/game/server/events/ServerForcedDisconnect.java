package com.insert_game_name.game.server.events;

/** Instructs a client to disconnect and optionally explains why. */
public record ServerForcedDisconnect(
    int reason,
    String message
) implements ServerEvent {
    /** Client requested the disconnect. */
    public static final int ACCEPTED_DISCONNECT = 0;
    /** Client exceeded the allowed heartbeat interval. */
    public static final int TIMEOUT = 1;
    /** Server or lobby administrator removed the client. */
    public static final int KICKED = 2;
    /** The lobby was closed. */
    public static final int LOBBY_CLOSED = 3;
    /** The server is shutting down. */
    public static final int SERVER_SHUTDOWN = 4;
}