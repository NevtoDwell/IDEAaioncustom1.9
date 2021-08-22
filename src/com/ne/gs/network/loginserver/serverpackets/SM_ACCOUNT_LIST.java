/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.serverpackets;

import java.util.Map;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.loginserver.LoginServerConnection;
import com.ne.gs.network.loginserver.LsServerPacket;

/**
 * GameServer packet that sends list of logged in accounts
 *
 * @author SoulKeeper
 */
public class SM_ACCOUNT_LIST extends LsServerPacket {

    /**
     * Map with loaded accounts
     */
    private final Map<Integer, AionConnection> accounts;

    /**
     * constructs new server packet with specified opcode.
     */
    public SM_ACCOUNT_LIST(Map<Integer, AionConnection> accounts) {
        super(0x04);
        this.accounts = accounts;
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeD(accounts.size());
        for (AionConnection ac : accounts.values()) {
            writeS(ac.getAccount().getName());
        }
    }
}
