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
 * This packet is response for CM_MAY_LOGIN_INTO_GAME
 *
 * @author -Nemesiss-
 */
public class SM_MAY_LOGIN_INTO_GAME extends AionServerPacket {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        /**
         * probably here is msg if fail.
         */
        writeD(0x00);
    }
}
