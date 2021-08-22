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
 * @author Avol
 */
public class SM_EXCHANGE_ADD_KINAH extends AionServerPacket {

    private final long itemCount;
    private final int action;

    public SM_EXCHANGE_ADD_KINAH(long itemCount, int action) {
        this.itemCount = itemCount;
        this.action = action;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(action); // 0 -self 1-other
        writeD((int) itemCount); // itemId
        writeD(0); // unk
    }
}
