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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.HashMap;

import com.insert_game_name.game.client.events.*;
import com.insert_game_name.game.server.events.*;
import com.insert_game_name.game.server.player.*;
import com.insert_game_name.utils.RecordUtil;

/** Coordinates players and processes client events for one lobby. */
public class Lobby implements Runnable {
    private static int count = 0;
    public int id;
    private final Map<String, Player> players = new HashMap<String, Player>();
    public LobbyPhase lobbyPhase;
    private ExecutorService playerPool;
    private LinkedBlockingQueue<PlayerEvent> inbox;
    private int version;
    private Integer playerCount = null;

    /**
     * Creates an empty lobby using the supplied executor for player I/O tasks.
     *
     * @param pool executor used to run player reader and writer tasks
     */
    public Lobby(ExecutorService pool) {
        id = count++;
        lobbyPhase = LobbyPhase.NEW;
        version = 0;
        playerPool = pool;
        inbox = new LinkedBlockingQueue<PlayerEvent>();
    }

    @Override 
    /** Processes queued player events until the lobby thread is interrupted. */
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {

                PlayerEvent playerEvent = inbox.take();
                ClientEvent event = playerEvent.event();
                Player player = playerEvent.player();

                switch (event) {
                    case ClientHello clientHello -> {
                        if (!player.connected) {
                            player.connected = true;
                            player.send(new ServerOk(clientHello.id(), null));
                        }
                    }
                    case ClientHeartbeat _ -> {
                        System.out.println(String.format("[info] User %s called a ClientHeartbeat", player.username));
                    }
                    case ClientDisconnectAlert _ -> {
                        System.out.println(String.format("[info] User %s called a ClientDisconnectAlert", player.username));
                        player.send(new ServerForcedDisconnect(ServerForcedDisconnect.ACCEPTED_DISCONNECT, null));
                    }
                    case ClientNotificationOffer notificationOffer -> {
                        System.out.println(String.format("[info] User %s called a ClientNotificationOffer", player.username));
                        if (!player.lobbyAdmin) break;
                        
                        if (processNotification(notificationOffer)) {
                            player.send(new ServerOk(notificationOffer.id(), null));
                            player.send(new ServerNotification(playerCount, this.state()));
                        } else {
                            player.send(new ServerError(notificationOffer.id(), ServerError.INVALID_NOTIFICATION, "The notification offer was deemed invalid by the server"));
                        }
                    }
                    case ClientOffer _ -> {
                        System.out.println(String.format("[info] User %s called a ClientOffer", player.username));
                        //TODO: pass offer to game
                    }
                    case ClientRequestLobbyState _ -> {
                        System.out.println(String.format("[info] User %s called a ClientRequestLobbyState", player.username));
                        player.send(new ServerNotification(null, this.state()));
                    }
                    case ClientRequestGameState _ -> {   
                        System.out.println(String.format("[info] User %s called a ClientRequestGameState", player.username));
                        player.send(new ServerUpdate(0, null));
                    }
                    case ClientMessage clientMessage -> {
                        System.out.println(String.format("[info] User %s called a ClientMessage with contents [%s]", player.username, clientMessage.content()));
                        //TODO: Send chat message
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Processes a notification offer from an admin
     * 
     * @return true if the lobby updated, false otherwise
     */
    private boolean processNotification(ClientNotificationOffer notification) {
        switch (lobbyPhase) {
            case NEW -> {
                // Accept the notification if the only differing field is the playerCount
                if (RecordUtil.differOnlyIn(this.state(), notification.state(), "playerCount")) {
                    setState(notification.state());
                    this.lobbyPhase = LobbyPhase.OPEN;
                }
            }
            case OPEN -> {}
            case CLOSED -> {}
            case IN_GAME -> {}
            case FINISHED -> {}
            case EMPTY -> {}
        }

        return false;
    }

    /**
     * Adds a player and starts that player's network tasks.
     *
     * @param player player to add
     */
    public void addPlayer(Player player) {
        System.out.println("[info] adding player " + player.username + " to lobby " + this.id);
        players.put(player.username, player);

        if (players.size() == 1) {
            player.lobbyAdmin = true;
        }

        player.start(playerPool, inbox);
    }

    /**
     * Stops and removes the player identified by the username.
     *
     * @param username username of the player to remove
     */
    public void removePlayer(String username) {
        players.get(username).disconnect(ServerForcedDisconnect.KICKED, "Removed from the Lobby");
        players.get(username).stop();
        players.remove(username);
    }

    /** @return a snapshot of the lobby phase and current players */
    public LobbyState state() {
        List<PlayerState> playerStates = new ArrayList<PlayerState>(players.size());
        
        for (Player player : players.values()) {
            playerStates.add(player.state());
        }
        
        LobbyState state = new LobbyState(version, lobbyPhase, playerCount, playerStates);

        return state;
    }

    private void setState(LobbyState state) {
        this.version++;
        this.lobbyPhase = state.lobbyPhase();
        this.playerCount = state.playerCount();
        // Ignore player states for now
    }
}
