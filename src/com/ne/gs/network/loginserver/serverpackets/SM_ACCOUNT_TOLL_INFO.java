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
 * @author xTz
 */
public class SM_ACCOUNT_TOLL_INFO extends LsServerPacket {

    private final long toll;

    private final String accountName;

    public SM_ACCOUNT_TOLL_INFO(long toll, String accountName) {
        super(0x09);
        this.accountName = accountName;
        this.toll = toll;
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeQ(toll);
        writeS(accountName);
    }
}
