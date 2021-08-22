/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * This packet is used to update current hp and max hp values.
 *
 * @author Luno
 */
public class SM_STATUPDATE_HP extends AionServerPacket {

    private final int currentHp;
    private final int maxHp;

    /**
     * @param currentHp
     * @param maxHp
     */
    public SM_STATUPDATE_HP(int currentHp, int maxHp) {
        this.currentHp = currentHp;
        this.maxHp = maxHp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(currentHp);
        writeD(maxHp);
    }

}
