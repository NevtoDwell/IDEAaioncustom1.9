/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.serverpackets;

import com.ne.gs.network.loginserver.LoginServerConnection;
import com.ne.gs.network.loginserver.LsServerPacket;

/**
 * In this packet GameServer is informing LoginServer that some account is no longer on GameServer [ie was disconencted]
 *
 * @author -Nemesiss-
 */
public class SM_ACCOUNT_DISCONNECTED extends LsServerPacket {

    /**
     * AccountId of account that is no longer on GameServer.
     */
    private final int accountId;

    /**
     * Constructs new instance of <tt>SM_ACCOUNT_DISCONNECTED </tt> packet.
     *
     * @param accountId
     *     account id
     */
    public SM_ACCOUNT_DISCONNECTED(int accountId) {
        super(0x03);
        this.accountId = accountId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeD(accountId);
    }
}
