/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.serverpackets.SM_TIME_CHECK;

/**
 * I dont know what this packet is doing - probably its ping/pong packet
 *
 * @author -Nemesiss-
 */
public class CM_TIME_CHECK extends AionClientPacket {

    /**
     * Nano time / 1000000
     */
    private int nanoTime;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        nanoTime = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection client = getConnection();
        int timeNow = (int) (System.nanoTime() / 1000000);
        @SuppressWarnings("unused") int diff = timeNow - nanoTime;
        client.sendPacket(new SM_TIME_CHECK(nanoTime));

        // log.info("CM_TIME_CHECK: " + nanoTime + " =?= " + timeNow + " dif: " + diff);
    }
}
