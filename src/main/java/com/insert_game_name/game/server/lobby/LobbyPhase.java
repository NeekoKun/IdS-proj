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

package com.insert_game_name.game.server.lobby;

/**
 * Represents the phases a game lobby can transition through:
 * <ul>
 *   <li>{@link #NEW}: created but not yet open for recruitment - the lobby admin didn't specify a player count.</li>
 *   <li>{@link #OPEN}: accepting players.</li>
 *   <li>{@link #CLOSED}: closed to new players and waiting to start.</li>
 *   <li>{@link #IN_GAME}: a game is currently in progress.</li>
 *   <li>{@link #FINISHED}: the game has ended.</li>
 *   <li>{@link #EMPTY}: empty and no longer active.</li>
 * </ul>
 */
public enum LobbyPhase {
    NEW,
    OPEN,
    CLOSED,
    IN_GAME,
    FINISHED,
    EMPTY;

    public boolean canGoTo(LobbyPhase target) {
        return switch (this) {
            case NEW -> target == OPEN;
            case OPEN -> target == CLOSED || target == IN_GAME;
            case CLOSED -> target == IN_GAME;
            case IN_GAME -> target == FINISHED;
            case FINISHED -> target == EMPTY;
            case EMPTY -> false;
        };
    }
}
