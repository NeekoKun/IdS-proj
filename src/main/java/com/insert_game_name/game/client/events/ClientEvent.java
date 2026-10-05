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
