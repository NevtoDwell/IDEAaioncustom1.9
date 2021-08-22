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
 * In this packet Server is sending response for CM_DELETE_CHARACTER.
 *
 * @author -Nemesiss-
 */
public class SM_DELETE_CHARACTER extends AionServerPacket {

    private final int playerObjId;
    private final int deletionTime;

    /**
     * Constructs new <tt>SM_DELETE_CHARACTER </tt> packet
     */
    public SM_DELETE_CHARACTER(int playerObjId, int deletionTime) {
        this.playerObjId = playerObjId;
        this.deletionTime = deletionTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        if (playerObjId != 0) {
            writeD(0x00);// unk
            writeD(playerObjId);
            writeD(deletionTime);
        } else {
            writeD(0x10);// unk
            writeD(0x00);
            writeD(0x00);
        }
    }
}
