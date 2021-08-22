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
 * @author ATracer
 */
public class SM_LEVEL_UPDATE extends AionServerPacket {

    private final int targetObjectId;
    private final int effect;
    private final int level;

    public SM_LEVEL_UPDATE(int targetObjectId, int effect, int level) {
        this.targetObjectId = targetObjectId;
        this.effect = effect;
        this.level = level;
    }

    /**
     * {@inheritDoc} dc
     */

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(targetObjectId);
        writeH(effect); // unk
        writeH(level);
        writeH(0x00); // unk
    }
}
