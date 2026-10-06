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
