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
 * This packet is response for CM_CHECK_NICKNAME.<br>
 * It sends client information if name can be used or not
 *
 * @author -Nemesiss-
 */
public class SM_NICKNAME_CHECK_RESPONSE extends AionServerPacket {

    /**
     * Value of response object
     */
    private final int value;

    /**
     * Constructs new <tt>SM_NICKNAME_CHECK_RESPONSE</tt> packet
     *
     * @param value
     *     Response value
     */
    public SM_NICKNAME_CHECK_RESPONSE(int value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        /**
         * Here is some msg: 0x00 = ok 0x0A = not ok and much more
         */
        writeC(value);
    }
}
