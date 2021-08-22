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
 * Created with IntelliJ IDEA.
 * User: Alexsis, thanks pixfid
 * Date: 14.12.12
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class SM_ACCOUNT_PROPERTIES extends AionServerPacket {

    public SM_ACCOUNT_PROPERTIES() {
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(0x03);
        writeH(0x00);
        writeD(0x00);
        writeD(0x00);
        writeD(0x8000);
        writeD(0x00);
        writeC(0x00);
        writeD(0x08);
        writeD(0x04);
    }
}
