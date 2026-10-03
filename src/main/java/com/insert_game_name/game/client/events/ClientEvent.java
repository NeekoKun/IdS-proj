package com.insert_game_name.game.client.events;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ClientHeartbeat.class,           name = "heartbeat"),
    @JsonSubTypes.Type(value = ClientMessage.class,             name = "message"),
    @JsonSubTypes.Type(value = ClientDisconnectAlert.class,     name = "disconnect_alert"),
    @JsonSubTypes.Type(value = ClientNotificationOffer.class,   name = "notification_offer"),
    @JsonSubTypes.Type(value = ClientOffer.class,               name = "offer"),
})
public sealed interface ClientEvent
    permits
        ClientHeartbeat,
        ClientHello,
        ClientMessage,
        ClientDisconnectAlert,
        ClientNotificationOffer,
        ClientOffer
    {}
