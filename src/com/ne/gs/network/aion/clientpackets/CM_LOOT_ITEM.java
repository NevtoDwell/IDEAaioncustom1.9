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
import com.ne.gs.services.drop.DropService;

/**
 * @author alexa026, ATracer
 */
public class CM_LOOT_ITEM extends AionClientPacket {

    private int targetObjectId;
    private int index;

    @Override
    protected void readImpl() {
        targetObjectId = readD();
        index = readC();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player == null) {
            return;
        }
        DropService.getInstance().requestDropItem(player, targetObjectId, index);
    }
}
