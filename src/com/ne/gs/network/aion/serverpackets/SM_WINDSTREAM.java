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

public class SM_WINDSTREAM extends AionServerPacket {

    private final int unk1; // {0, 2, 3, 6, 7, 8}
    private final int unk2;

    public SM_WINDSTREAM(int unk1, int unk2) {
        this.unk1 = unk1;
        this.unk2 = unk2;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(unk1);
        writeC(unk2);
    }
}
