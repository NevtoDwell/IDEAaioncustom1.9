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
 * @author xavier
 */
public class SM_MACRO_RESULT extends AionServerPacket {

    public static SM_MACRO_RESULT SM_MACRO_CREATED = new SM_MACRO_RESULT(0x00);
    public static SM_MACRO_RESULT SM_MACRO_DELETED = new SM_MACRO_RESULT(0x01);

    private final int code;

    private SM_MACRO_RESULT(int code) {
        this.code = code;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(code);
    }
}
