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
 * dunno wtf this packet is doing. Not sure about id/name
 *
 * @author -Nemesiss-
 */
public class SM_ENTER_WORLD_CHECK extends AionServerPacket {

    private byte msg = 0x00;

    public SM_ENTER_WORLD_CHECK(byte msg) {
        this.msg = msg;
    }

    public SM_ENTER_WORLD_CHECK() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeC(msg);
        writeC(0x00);
        writeC(0x00);
    }
}
