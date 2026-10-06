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

package com.insert_game_name.game.client.events;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ClientHello.class,               name = "hello"),
    @JsonSubTypes.Type(value = ClientHeartbeat.class,           name = "heartbeat"),
    @JsonSubTypes.Type(value = ClientMessage.class,             name = "message"),
    @JsonSubTypes.Type(value = ClientDisconnectAlert.class,     name = "disconnect_alert"),
    @JsonSubTypes.Type(value = ClientNotificationOffer.class,   name = "notification_offer"),
    @JsonSubTypes.Type(value = ClientOffer.class,               name = "offer"),
    @JsonSubTypes.Type(value = ClientRequestLobbyState.class,   name = "request_lobby_state"),
    @JsonSubTypes.Type(value = ClientRequestGameState.class,    name = "request_game_state"),
})
/** Base type for all events sent from a client to the server. */
public sealed interface ClientEvent
    permits
        ClientHeartbeat,
        ClientMessage,
        ClientDisconnectAlert,
        ClientNotificationOffer,
        ClientOffer,
        ClientHello,
        ClientRequestLobbyState,
        ClientRequestGameState
    {}
