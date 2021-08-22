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
 * @author Sweetkr
 */
public class SM_DP_INFO extends AionServerPacket {

    private final int playerObjectId;
    private final int currentDp;

    public SM_DP_INFO(int playerObjectId, int currentDp) {
        this.playerObjectId = playerObjectId;
        this.currentDp = currentDp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjectId);
        writeH(currentDp);
    }

}
