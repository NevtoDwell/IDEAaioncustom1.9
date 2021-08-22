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
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * Handler for "/loc" command
 *
 * @author SoulKeeper
 * @author EvilSpirit
 */
public class CM_CLIENT_COMMAND_LOC extends AionClientPacket {

    /**
     * Nothing to do
     */
    @Override
    protected void readImpl() {
        // empty
    }

    /**
     * Logging
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        sendPacket(SM_SYSTEM_MESSAGE.STR_CMD_LOCATION_DESC(player.getWorldId(), player.getX(), player.getY(), player.getZ()));
    }
}
