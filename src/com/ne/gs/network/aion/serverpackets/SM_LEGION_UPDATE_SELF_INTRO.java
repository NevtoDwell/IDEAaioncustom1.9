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
public class SM_LEGION_UPDATE_SELF_INTRO extends AionServerPacket {

    private final String selfintro;
    private final int playerObjId;

    public SM_LEGION_UPDATE_SELF_INTRO(int playerObjId, String selfintro) {
        this.selfintro = selfintro;
        this.playerObjId = playerObjId;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjId);
        writeS(selfintro);
    }
}
