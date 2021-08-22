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
 * @author xTz
 */
public class SM_TOLL_INFO extends AionServerPacket {

    private final long tollCount;

    public SM_TOLL_INFO(long tollCount) {
        this.tollCount = tollCount;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeQ(tollCount);
    }
}
