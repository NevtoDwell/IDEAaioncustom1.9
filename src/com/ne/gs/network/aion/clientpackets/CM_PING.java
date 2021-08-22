/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_PONG;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author -Nemesiss- modified by Undertrey
 */
public class CM_PING extends AionClientPacket {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        readH(); // unk
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        long lastMS = getConnection().getLastPingTimeMS();

        if (lastMS > 0 && player != null) {
            long pingInterval = System.currentTimeMillis() - lastMS;
            // PingInterval should be 3min (180000ms)
            if (pingInterval < SecurityConfig.PING_INTERVAL * 1000) {// client timer cheat
                AuditLogger.info(player, "Possible client timer cheat kicking player: " + pingInterval + ", ip=" + getConnection().getIP());
                if (SecurityConfig.SECURITY_ENABLE) {
                    player.sendMsg("You have been triggered Speed Hack detection so you're disconnected.");
                    getConnection().closeNow();
                }
            }
        }
        getConnection().setLastPingTimeMS(System.currentTimeMillis());
        sendPacket(new SM_PONG());
    }
}
