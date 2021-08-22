/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/*
 * @author sweetkr, Sarynth
 */
public class SM_SET_BIND_POINT extends AionServerPacket {

    private final int mapId;
    private final float x;
    private final float y;
    private final float z;
    private final Kisk kisk;

    public SM_SET_BIND_POINT(int mapId, float x, float y, float z, Player player) {
        this.mapId = mapId;
        this.x = x;
        this.y = y;
        this.z = z;
        kisk = player.getKisk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        // Appears 0x04 if bound to a kisk. 0x00 if not.
        writeC((kisk == null ? 0x00 : 0x04));

        writeC(0x01);// unk
        writeD(mapId);// map id
        writeF(x); // coordinate x
        writeF(y); // coordinate y
        writeF(z); // coordinate z
        writeD((kisk == null ? 0x00 : (kisk.isActive() ? kisk.getObjectId() : 0))); // kisk object id
    }

}
