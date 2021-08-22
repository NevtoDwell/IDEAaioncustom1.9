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
 * @author Aionchs-Wylovech
 */
public class SM_LS_CONTROL extends LsServerPacket {

    private final String accountName;

    private final String adminName;

    private final String playerName;

    private final int param;

    private final int type;

    public SM_LS_CONTROL(String accountName, String playerName, String adminName, int param, int type) {
        super(0x05);
        this.accountName = accountName;
        this.param = param;
        this.playerName = playerName;
        this.adminName = adminName;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeC(type);
        writeS(adminName);
        writeS(accountName);
        writeS(playerName);
        writeC(param);
    }
}
