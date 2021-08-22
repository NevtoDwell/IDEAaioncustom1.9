/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_CHAT_INIT;
import com.ne.gs.network.chatserver.ChatServer;
import com.ne.gs.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ATracer
 */
public final class ChatService {

    protected static final Logger Log = LoggerFactory.getLogger(ChatService.class);

    private static byte[] ip = {127, 0, 0, 1};
    private static int port = 10241;

    /**
     * Disonnect from chat server
     *
     * @param player
     */
    public static void onPlayerLogout(Player player) {
        ChatServer.getInstance().sendPlayerLogout(player);
    }

    /**
     * @param playerId
     * @param token
     */
    public static void playerAuthed(int playerId, byte[] token) {
        Player player = World.getInstance().findPlayer(playerId);
        if (player != null) {
            player.sendPck(new SM_CHAT_INIT(token));
        }
        else{
            Log.warn("Chat register: Player {} not found", playerId);
        }
    }

    /**
     * @return the ip
     */
    public static byte[] getIp() {
        return ip;
    }

    /**
     * @return the port
     */
    public static int getPort() {
        return port;
    }

    /**
     */
    public static void setIp(byte[] _ip) {
        ip = _ip;
    }

    /**
     */
    public static void setPort(int _port) {
        port = _port;
    }
}
