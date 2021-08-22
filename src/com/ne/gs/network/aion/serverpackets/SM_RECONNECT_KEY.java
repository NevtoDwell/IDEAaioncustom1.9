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
 * Response for CM_RECONNECT_AUTH with key that will be use for authentication at LoginServer.
 *
 * @author -Nemesiss-
 */
public class SM_RECONNECT_KEY extends AionServerPacket {

    /**
     * key for reconnection - will be used for authentication
     */
    private final int key;

    /**
     * Constructs new <tt>SM_RECONNECT_KEY</tt> packet
     *
     * @param key
     *     key for reconnection
     */
    public SM_RECONNECT_KEY(int key) {
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeC(0x00);
        writeD(key);
    }
}
