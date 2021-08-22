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
 * @author Lyahim
 */
public class SM_LEAVE_GROUP_MEMBER extends AionServerPacket {

    @Override
    protected void writeImpl(AionConnection con) {

        writeD(0x00);
        writeC(0x00);
        writeD(0x3F);
        writeD(0x00);
        writeH(0x00);
    }
}
