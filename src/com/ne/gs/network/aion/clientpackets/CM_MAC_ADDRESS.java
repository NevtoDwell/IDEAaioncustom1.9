/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.LoggerFactory;

import com.ne.gs.network.BannedMacManager;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * In this packet client is sending Mac Address - haha.
 *
 * @author -Nemesiss-, KID
 */
public class CM_MAC_ADDRESS extends AionClientPacket {

    /**
     * Mac Addres send by client in the same format as: ipconfig /all [ie: xx-xx-xx-xx-xx-xx]
     */
    private String macAddress;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        readC();
        short counter = (short) readH();
        for (short i = 0; i < counter; i++) {
            readD();
        }
        macAddress = readS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        if (BannedMacManager.getInstance().isBanned(macAddress)) {
            // TODO some information packets
            getConnection().closeNow();
            LoggerFactory.getLogger(CM_MAC_ADDRESS.class).info("[MAC_AUDIT] " + macAddress + " (" + getConnection().getIP() + ") was kicked due to mac ban");
        } else {
            getConnection().setMacAddress(macAddress);
        }
    }
}
