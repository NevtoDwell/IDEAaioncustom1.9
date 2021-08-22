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
 * This packet is response for CM_QUIT
 *
 * @author -Nemesiss-
 */
public class SM_QUIT_RESPONSE extends AionServerPacket {

    private boolean edit_mode = false;

    public SM_QUIT_RESPONSE() {
    }

    public SM_QUIT_RESPONSE(boolean edit_mode) {
        this.edit_mode = edit_mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(edit_mode ? 2 : 1);// 1 normal, 2 plastic surgery/gender switch
        writeC(0x00);// unk
        writeC(0xFF);
        writeC(0XFF);
        writeC(0XFF);
        writeC(0XFF);
    }
}
