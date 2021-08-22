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
 * This packet is used to update mp / max mp value.
 *
 * @author Luno
 */
public class SM_STATUPDATE_MP extends AionServerPacket {

    private final int currentMp;
    private final int maxMp;

    /**
     * @param currentMp
     * @param maxMp
     */
    public SM_STATUPDATE_MP(int currentMp, int maxMp) {
        this.currentMp = currentMp;
        this.maxMp = maxMp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(currentMp);
        writeD(maxMp);
    }

}
