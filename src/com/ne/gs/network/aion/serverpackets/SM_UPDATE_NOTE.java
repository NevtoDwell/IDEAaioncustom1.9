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
 * @author xavier
 */
public class SM_UPDATE_NOTE extends AionServerPacket {

    private final int targetObjId;
    private final String note;

    public SM_UPDATE_NOTE(int targetObjId, String note) {
        this.targetObjId = targetObjId;
        this.note = note;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(targetObjId);
        writeS(note);
    }
}
