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

public class SM_ONLINE_STATUS extends AionServerPacket {

    byte status;

    public SM_ONLINE_STATUS(byte status) {
        this.status = status;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(status);
    }
}
