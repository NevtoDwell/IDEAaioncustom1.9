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
public class SM_SUMMON_OWNER_REMOVE extends AionServerPacket {

    private final int summonObjId;

    public SM_SUMMON_OWNER_REMOVE(int summonObjId) {
        this.summonObjId = summonObjId;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(summonObjId);
    }
}
