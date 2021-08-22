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
public class SM_USE_OBJECT extends AionServerPacket {

    private final int playerObjId;
    private final int targetObjId;
    private final int time;
    private final int actionType;

    public SM_USE_OBJECT(int playerObjId, int targetObjId, int time, int actionType) {
        super();
        this.playerObjId = playerObjId;
        this.targetObjId = targetObjId;
        this.time = time;
        this.actionType = actionType;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjId);
        writeD(targetObjId);
        writeD(time);
        writeC(actionType);
    }
}
