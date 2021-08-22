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
 * @author xTz
 */
public class SM_INSTANCE_STAGE_INFO extends AionServerPacket {

    private final int type;
    private final int event;
    private final int unk;

    public SM_INSTANCE_STAGE_INFO(int type, int event, int unk) {
        this.type = type;
        this.event = event;
        this.unk = unk;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(type);
        writeD(0);
        writeH(event);
        writeH(unk);
    }
}
