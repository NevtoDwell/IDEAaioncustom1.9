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
 * This packet is used to update current dp (divine points) value.
 *
 * @author Luno
 */
public class SM_STATUPDATE_DP extends AionServerPacket {

    private final int currentDp;

    /**
     * @param currentDp
     */
    public SM_STATUPDATE_DP(int currentDp) {
        this.currentDp = currentDp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeH(currentDp);
    }

}
