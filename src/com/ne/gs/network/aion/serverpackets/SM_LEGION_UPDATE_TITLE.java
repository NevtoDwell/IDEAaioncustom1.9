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
 * @author sweetkr
 */
public class SM_LEGION_UPDATE_TITLE extends AionServerPacket {

    private final int objectId;
    private final int legionId;
    private final String legionName;
    private final int rank;

    public SM_LEGION_UPDATE_TITLE(int objectId, int legionId, String legionName, int rank) {
        this.objectId = objectId;
        this.legionId = legionId;
        this.legionName = legionName;
        this.rank = rank;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(objectId);
        writeD(legionId);
        writeS(legionName);
        writeC(rank); // 0: commander(?), 1: centurion, 2: soldier
    }
}
