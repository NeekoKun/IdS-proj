/*
 * Copyright (C) 2026 Leonardo Ricci Mingani (NeekoKun)
 *
 * This file is part of IdS-proj.
 *
 * IdS-proj is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IdS-proj is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IdS-proj. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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