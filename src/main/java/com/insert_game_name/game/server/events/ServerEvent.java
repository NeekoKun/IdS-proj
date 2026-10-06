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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ServerHeartbeat.class,        name = "heartbeat"),
    @JsonSubTypes.Type(value = ServerMessage.class,          name = "message"),
    @JsonSubTypes.Type(value = ServerForcedDisconnect.class, name = "forced_disconnect"),
    @JsonSubTypes.Type(value = ServerNotification.class,     name = "notification"),
    @JsonSubTypes.Type(value = ServerUpdate.class,           name = "update"),
    @JsonSubTypes.Type(value = ServerError.class,       name = "error"),
    @JsonSubTypes.Type(value = ServerOk.class,               name = "ok")
})
/** Base type for all events sent from the server to a client. */
public sealed interface ServerEvent 
        permits 
                ServerHeartbeat, 
                ServerMessage, 
                ServerForcedDisconnect, 
                ServerNotification, 
                ServerUpdate, 
                ServerError, 
                ServerOk 
        {}
