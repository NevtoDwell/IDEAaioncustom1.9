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

/**
 * @author xTz
 */
public class CM_INSTANCE_LEAVE extends AionClientPacket {

    @Override
    protected void readImpl() {
        // nothing to read
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player.isInInstance()) {
            player.getPosition().getWorldMapInstance().getInstanceHandler().onExitInstance(player);
        }
    }
}
