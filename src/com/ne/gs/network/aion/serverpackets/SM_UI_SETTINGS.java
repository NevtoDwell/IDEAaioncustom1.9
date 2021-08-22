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
 * @author ATracer
 */
public class SM_UI_SETTINGS extends AionServerPacket {

    private final byte[] data;
    private final int type;

    /**
     * Constructs new <tt>SM_CHARACTER_UI </tt> packet
     */
    public SM_UI_SETTINGS(byte[] data, int type) {
        this.data = data;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeH(type);
        writeC(0x1C);
        writeB(data);
    }

}
