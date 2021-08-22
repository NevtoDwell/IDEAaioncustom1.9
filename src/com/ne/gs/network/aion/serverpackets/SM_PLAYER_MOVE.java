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
 * @author cura, Alex
 */
public class SM_PLAYER_MOVE extends AionServerPacket {

    private final float x;
    private final float y;
    private final float z;
    private final byte heading;
    private final int state;

    public SM_PLAYER_MOVE(float x, float y, float z, byte heading, int state) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.heading = heading;
        this.state = state;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeF(x);
        writeF(y);
        writeF(z);
        writeC(heading);
        writeC(state);
    }
}
