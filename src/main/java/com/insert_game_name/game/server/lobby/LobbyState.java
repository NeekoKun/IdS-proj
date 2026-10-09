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

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.insert_game_name.game.server.player.PlayerState;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
/**
 * Immutable snapshot of a lobby and its players.
 *
 * @param version the update counter of this lobby
 * @param lobbyPhase the current lobby phase
 * @param playerCount the target number of player in the lobby to reach before the game can start
 * @param playerState the states of the players in the lobby
 */
public record LobbyState (
    int version,
    LobbyPhase lobbyPhase,
    Integer playerCount,
    List<PlayerState> playerState
) {}