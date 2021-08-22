/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.clientpackets;

import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.LsClientPacket;

/**
 * In this packet LoginServer is sending response for SM_ACCOUNT_RECONNECT_KEY with account name and reconnectionKey.
 *
 * @author -Nemesiss-
 */
public class CM_ACCOUNT_RECONNECT_KEY extends LsClientPacket {

    public CM_ACCOUNT_RECONNECT_KEY(int opCode) {
        super(opCode);
    }

    /**
     * accountId of account that will be reconnecting.
     */
    private int accountId;
    /**
     * ReconnectKey that will be used for authentication.
     */
    private int reconnectKey;

    /**
     * {@inheritDoc}
     */
    @Override
    public void readImpl() {
        accountId = readD();
        reconnectKey = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runImpl() {
        LoginServer.getInstance().authReconnectionResponse(accountId, reconnectKey);
    }
}
