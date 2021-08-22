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
 * @author Simple
 */
public class SM_LEGION_LEAVE_MEMBER extends AionServerPacket {

    private final String name;
    private String name1;
    private final int playerObjId;
    private final int msgId;

    public SM_LEGION_LEAVE_MEMBER(int msgId, int playerObjId, String name) {
        this.msgId = msgId;
        this.playerObjId = playerObjId;
        this.name = name;
    }

    public SM_LEGION_LEAVE_MEMBER(int msgId, int playerObjId, String name, String name1) {
        this.msgId = msgId;
        this.playerObjId = playerObjId;
        this.name = name;
        this.name1 = name1;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjId);
        writeC(0x00); // isMember ? 1 : 0
        writeD(0x00); // unix time for log off
        writeD(msgId);
        writeS(name);
        writeS(name1);
    }
}
