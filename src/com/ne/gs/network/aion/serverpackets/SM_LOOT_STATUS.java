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
 * @author alexa026
 */
@Deprecated
public class SM_LOOT_STATUS extends AionServerPacket {

    private final int targetObjectId;
    private final int state;

    public SM_LOOT_STATUS(int targetObjectId, int state) {
        this.targetObjectId = targetObjectId;
        this.state = state;
    }

    /**
     * {@inheritDoc} dc
     */

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(targetObjectId);
        writeC(state);
    }
}
