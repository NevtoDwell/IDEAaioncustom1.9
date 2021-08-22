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
 * @author Nemesiss
 */
public class SM_HEADING_UPDATE extends AionServerPacket {

    private final int objectId;
    private final int heading;

    public SM_HEADING_UPDATE(int objectId, int heading) {
        this.objectId = objectId;
        this.heading = heading;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(objectId);
        writeC(heading);
    }
}
