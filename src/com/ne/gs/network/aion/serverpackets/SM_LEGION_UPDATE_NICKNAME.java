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
public class SM_LEGION_UPDATE_NICKNAME extends AionServerPacket {

    private final int playerObjId;
    private final String newNickname;

    public SM_LEGION_UPDATE_NICKNAME(int playerObjId, String newNickname) {
        this.playerObjId = playerObjId;
        this.newNickname = newNickname;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjId);
        writeS(newNickname);
    }
}