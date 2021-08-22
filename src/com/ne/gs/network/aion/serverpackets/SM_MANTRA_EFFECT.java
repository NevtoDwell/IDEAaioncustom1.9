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
 * @author Sweetkr
 */
public class SM_MANTRA_EFFECT extends AionServerPacket {

    private final Player player;
    private final int subEffectId;

    public SM_MANTRA_EFFECT(Player player, int subEffectId) {
        this.player = player;
        this.subEffectId = subEffectId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(0x00);// unk
        writeD(player.getObjectId());
        writeH(subEffectId);
    }
}
