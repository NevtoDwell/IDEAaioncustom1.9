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

/**
 * @author xavier Packet sent by client when player may quit game in 10 seconds
 */
public class CM_MAY_QUIT extends AionClientPacket {

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.packet.BaseClientPacket#readImpl()
     */
    @Override
    protected void readImpl() {
        // empty
    }

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.packet.BaseClientPacket#runImpl()
     */
    @Override
    protected void runImpl() {
        // Nothing to do
    }

}
