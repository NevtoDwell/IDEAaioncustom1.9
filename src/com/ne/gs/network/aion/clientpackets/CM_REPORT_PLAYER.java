/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * Received when a player reports another player with /ReportAutoHunting
 *
 * @author Jego
 */
public class CM_REPORT_PLAYER extends AionClientPacket {

    private String player;

    @Override
    protected void readImpl() {
        readB(1); // unknown byte.
        player = readS(); // the name of the reported person.
    }

    @Override
    protected void runImpl() {
        Player p = getConnection().getActivePlayer();
        AuditLogger.info(p, "Reports the player: " + player);
    }

}
