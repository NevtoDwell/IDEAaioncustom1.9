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
import com.ne.gs.world.WorldPosition;

/**
 * @author ATracer
 */
public class SM_CHANNEL_INFO extends AionServerPacket {

    int instanceCount = 0;
    int currentChannel = 0;

    /**
     * @param position
     */
    public SM_CHANNEL_INFO(WorldPosition position) {
        instanceCount = position.getInstanceCount();
        currentChannel = position.getInstanceId() - 1;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(currentChannel);
        writeD(instanceCount);
    }
}
