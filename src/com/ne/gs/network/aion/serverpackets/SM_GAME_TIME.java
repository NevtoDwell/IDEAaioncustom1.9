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
import com.ne.gs.utils.gametime.GameTimeManager;

/**
 * Sends the current time in the server in minutes since 1/1/00 00:00:00
 *
 * @author Ben
 */
public class SM_GAME_TIME extends AionServerPacket {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(GameTimeManager.getGameTime().getTime()); // Minutes since 1/1/00 00:00:00
    }

}
