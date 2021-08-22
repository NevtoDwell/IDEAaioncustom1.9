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
import com.ne.gs.network.loginserver.LoginServer;

/**
 * In this packets aion client is authenticating himself by providing accountId and rest of sessionKey - we will check if its valid at login server side.
 *
 * @author -Nemesiss-
 */
// TODO: L2AUTH? Really? :O
public class CM_L2AUTH_LOGIN_CHECK extends AionClientPacket {

    /**
     * playOk2 is part of session key - its used for security purposes we will check if this is the key what login server sends.
     */
    private int playOk2;
    /**
     * playOk1 is part of session key - its used for security purposes we will check if this is the key what login server sends.
     */
    private int playOk1;
    /**
     * accountId is part of session key - its used for authentication we will check if this accountId is matching any waiting account login server side and
     * check if rest of session key is ok.
     */
    private int accountId;
    /**
     * loginOk is part of session key - its used for security purposes we will check if this is the key what login server sends.
     */
    private int loginOk;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        playOk2 = readD();
        playOk1 = readD();
        accountId = readD();
        loginOk = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        LoginServer.getInstance().requestAuthenticationOfClient(accountId, getConnection(), loginOk, playOk1, playOk2);
    }
}
