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
 * @author cura
 */
public class SM_LEGION_SEND_EMBLEM_DATA extends AionServerPacket {

    private final int size;
    private final byte[] data;

    public SM_LEGION_SEND_EMBLEM_DATA(int size, byte[] data) {
        this.size = size;
        this.data = data;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(size);
        writeB(data);
    }
}
