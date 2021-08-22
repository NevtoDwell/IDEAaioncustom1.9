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
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_SPAWN;
import com.ne.gs.world.WorldMapType;

public class CM_TELEPORT_DONE extends AionClientPacket {

    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (WorldMapType.of(player.getWorldId()).isPersonal()) {
            player.sendPck(new SM_PLAYER_SPAWN(player));
        }
    }
}
