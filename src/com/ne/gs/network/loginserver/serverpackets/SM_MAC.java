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
 * @author nrg
 */
public class SM_MAC extends LsServerPacket {

    private final int accountId;
    private final String address;

    public SM_MAC(int accountId, String address) {
        super(13);
        this.accountId = accountId;
        this.address = address;
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeD(accountId);
        writeS(address);
    }
}
