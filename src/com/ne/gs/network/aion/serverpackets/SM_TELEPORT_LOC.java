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
 * This packet is used to teleport player
 *
 * @author Luno , orz
 */
public class SM_TELEPORT_LOC extends AionServerPacket {

    private final int portAnimation;
    private final int mapId;
    private final int instanceId;
    private final float x;
    private final float y;
    private final float z;
    private final int heading;
    private final boolean isInstance;

    public SM_TELEPORT_LOC(boolean isInstance, int instanceId, int mapId, float x, float y, float z, int heading,
                           int portAnimation) {
        this.isInstance = isInstance;
        this.instanceId = instanceId;
        this.mapId = mapId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.heading = heading;
        this.portAnimation = portAnimation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeC(portAnimation);
        writeH(mapId & 0xFFFF);
        writeD(isInstance ? instanceId : mapId);
        writeF(x);
        writeF(y);
        writeF(z);
        writeC(heading);
    }
}
