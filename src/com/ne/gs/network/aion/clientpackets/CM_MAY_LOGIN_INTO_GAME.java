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
import com.ne.gs.network.aion.serverpackets.SM_MAY_LOGIN_INTO_GAME;

/**
 * In this packets aion client is asking if may login into game [ie start playing].
 *
 * @author -Nemesiss-
 */
public class CM_MAY_LOGIN_INTO_GAME extends AionClientPacket {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection client = getConnection();
        // TODO! check if may login into game [play time etc]
        client.sendPacket(new SM_MAY_LOGIN_INTO_GAME());
    }
}
