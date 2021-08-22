/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.sql.Timestamp;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * I have no idea wtf is this
 *
 * @author -Nemesiss-
 */
public class SM_TIME_CHECK extends AionServerPacket {

    // Don't be fooled with empty class :D
    // This packet is just sending opcode, without any content

    // 1.5.x sending 8 bytes

    private final int nanoTime;
    private final int time;
    private final Timestamp dateTime;

    public SM_TIME_CHECK(int nanoTime) {
        dateTime = new Timestamp((new java.util.Date()).getTime());
        this.nanoTime = nanoTime;
        time = (int) dateTime.getTime();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(time);
        writeD(nanoTime);

    }
}
