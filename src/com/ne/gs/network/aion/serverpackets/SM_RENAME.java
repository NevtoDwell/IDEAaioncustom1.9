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
 * @author Rhys2002
 */
public class SM_RENAME extends AionServerPacket {

    private final int playerObjectId;
    private final String oldName;
    private final String newName;

    public SM_RENAME(int playerObjectId, String oldName, String newName) {
        this.playerObjectId = playerObjectId;
        this.oldName = oldName;
        this.newName = newName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(0); // unk
        writeD(0); // unk - 0 or 3
        writeD(playerObjectId);
        writeS(oldName);
        writeS(newName);
    }
}
