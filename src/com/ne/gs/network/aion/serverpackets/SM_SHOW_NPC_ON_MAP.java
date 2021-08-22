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
 * @author Lyahim
 */
public class SM_SHOW_NPC_ON_MAP extends AionServerPacket {

    private final int npcid, worldid;
    private final float x, y, z;

    public SM_SHOW_NPC_ON_MAP(int npcid, int worldid, float x, float y, float z) {
        this.npcid = npcid;
        this.worldid = worldid;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(npcid);
        writeD(worldid);
        writeD(worldid);
        writeF(x);
        writeF(y);
        writeF(z);
    }
}
