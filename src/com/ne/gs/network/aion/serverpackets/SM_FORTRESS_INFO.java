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

public class SM_FORTRESS_INFO extends AionServerPacket {

    private final int locationId;
    private final boolean teleportStatus;

    public SM_FORTRESS_INFO(int locationId, boolean teleportStatus) {
        this.locationId = locationId;
        this.teleportStatus = teleportStatus;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(locationId);
        writeC(teleportStatus ? 1 : 0);
    }

}
