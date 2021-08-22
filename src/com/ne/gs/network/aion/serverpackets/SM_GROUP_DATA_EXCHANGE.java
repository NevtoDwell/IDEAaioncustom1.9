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

public class SM_GROUP_DATA_EXCHANGE extends AionServerPacket {

    private final byte[] byteData;
    private final int action;
    private int unk2;

    public SM_GROUP_DATA_EXCHANGE(byte[] byteData, int action, int unk2) {
        this.action = action;
        this.byteData = byteData;
        this.unk2 = unk2;
    }

    public SM_GROUP_DATA_EXCHANGE(byte[] byteData) {
        action = 1;
        this.byteData = byteData;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(action);

        if (action != 1) {
            writeC(unk2);
        }
        writeD(byteData.length);
        writeB(byteData);
    }
}
