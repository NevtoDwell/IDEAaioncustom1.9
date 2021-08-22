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
public class SM_CHAT_INIT extends AionServerPacket {

    private final byte[] token;

    /**
     * @param token
     */
    public SM_CHAT_INIT(byte[] token) {
        this.token = token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(token.length);
        writeB(token);
    }
}
