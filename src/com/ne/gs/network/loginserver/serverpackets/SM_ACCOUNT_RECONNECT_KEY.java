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
 * This packet is sended by GameServer when player is requesting fast reconnect to login server. LoginServer in response will send reconectKey.
 *
 * @author -Nemesiss-
 */
public class SM_ACCOUNT_RECONNECT_KEY extends LsServerPacket {

    /**
     * AccountId of client that is requested reconnection to LoginServer.
     */
    private final int accountId;

    /**
     * Constructs new instance of <tt>SM_ACCOUNT_RECONNECT_KEY </tt> packet.
     *
     * @param accountId
     *     account identifier.
     */
    public SM_ACCOUNT_RECONNECT_KEY(int accountId) {
        super(0x02);
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
