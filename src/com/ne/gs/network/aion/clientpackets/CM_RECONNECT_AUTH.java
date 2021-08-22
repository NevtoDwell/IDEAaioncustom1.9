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
import com.ne.gs.network.loginserver.LoginServer;

/**
 * In this packets aion client is asking for fast reconnection to LoginServer.
 *
 * @author -Nemesiss-
 */
public class CM_RECONNECT_AUTH extends AionClientPacket {

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
        // TODO! check if may reconnect
        LoginServer.getInstance().requestAuthReconnection(client);
    }
}
