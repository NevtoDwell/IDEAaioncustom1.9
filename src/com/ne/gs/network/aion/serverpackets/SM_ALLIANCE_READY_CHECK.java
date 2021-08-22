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
 * @author Sarynth (Thx Rhys2002 for Packets)
 */
public class SM_ALLIANCE_READY_CHECK extends AionServerPacket {

    private final int playerObjectId;
    private final int statusCode;

    public SM_ALLIANCE_READY_CHECK(int playerObjectId, int statusCode) {
        this.playerObjectId = playerObjectId;
        this.statusCode = statusCode;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjectId);
        writeC(statusCode);
    }

}
