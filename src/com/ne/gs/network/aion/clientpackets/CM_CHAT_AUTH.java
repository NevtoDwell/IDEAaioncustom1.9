/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.chatserver.ChatServer;

/**
 * Client sends this only once.
 *
 * @author Luno
 */
public class CM_CHAT_AUTH extends AionClientPacket {

    @Override
    protected void readImpl() {
        @SuppressWarnings("unused") int objectId = readD(); // lol NC
        @SuppressWarnings("unused") byte[] macAddress = readB(6);
    }

    @Override
    protected void runImpl() {
        if (GSConfig.ENABLE_CHAT_SERVER) {
            // this packet is sent sometimes after logout from world
            Player player = getConnection().getActivePlayer();
            if (!player.isInPrison() && !player.isGagged()) {
                ChatServer.getInstance().sendPlayerLoginRequst(player);
            }
        }
    }
}
