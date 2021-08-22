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
public class SM_LOGIN_QUEUE extends AionServerPacket {

    private final int waitingPosition; // What is the player's position in line
    private final int waitingTime; // Per waiting position in seconds
    private final int waitingCount; // How many are waiting in line

    private SM_LOGIN_QUEUE() {
        waitingPosition = 5;
        waitingTime = 60;
        waitingCount = 50;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(waitingPosition);
        writeD(waitingTime);
        writeD(waitingCount);
    }
}
