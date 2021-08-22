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
 * This packet is request kicking player.
 *
 * @author -Nemesiss-
 */
public class CM_REQUEST_KICK_ACCOUNT extends LsClientPacket {

    public CM_REQUEST_KICK_ACCOUNT(int opCode) {
        super(opCode);
    }

    /**
     * account id of account that login server request to kick.
     */
    private int accountId;

    @Override
    public void readImpl() {
        accountId = readD();
    }

    @Override
    public void runImpl() {
        LoginServer.getInstance().kickAccount(accountId);
    }
}
