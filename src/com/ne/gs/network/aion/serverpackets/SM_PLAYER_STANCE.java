/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author prix
 */
public class SM_PLAYER_STANCE extends AionServerPacket {

    private final Player player;
    private final int state;

    public SM_PLAYER_STANCE(Player player, int state) {
        this.player = player;
        this.state = state; // 0 = off, 1 = block, flight, glide, jump, etc.
        // 2 = stationary object
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(player.getObjectId());
        writeC(state);
    }
}
