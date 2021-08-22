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
 * @author orz, Sarynth
 */
public class SM_SELL_ITEM extends AionServerPacket {

    private final int targetObjectId;
    private final int sellPercentage;

    public SM_SELL_ITEM(int targetObjectId, int sellPercentage) {

        this.sellPercentage = sellPercentage;
        this.targetObjectId = targetObjectId;

    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected void writeImpl(AionConnection con) {

        writeD(targetObjectId);
        writeD(sellPercentage); // Buy Price * (sellPercentage / 100) = Display price.

    }
}
