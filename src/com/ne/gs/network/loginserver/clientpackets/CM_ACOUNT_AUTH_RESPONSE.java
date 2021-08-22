/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.clientpackets;

import java.sql.Timestamp;

import com.ne.commons.shared.AccountTime;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.LsClientPacket;

/**
 * In this packet LoginServer is answering on GameServer request about valid authentication data and also sends account name of user that is authenticating on
 * GameServer.
 *
 * @author -Nemesiss-
 */
public class CM_ACOUNT_AUTH_RESPONSE extends LsClientPacket {

    public CM_ACOUNT_AUTH_RESPONSE(int opCode) {
        super(opCode);
    }

    /**
     * accountId
     */
    private int accountId;

    /**
     * result - true = authed
     */
    private boolean result;

    /**
     * accountName [if response is ok]
     */
    private String accountName;
    /**
     * accountTime
     */
    private AccountTime accountTime;
    /**
     * access level - regular/gm/admin
     */
    private byte accessLevel;
    /**
     * Membership - regular/premium
     */
    private byte membership;

    /**
     * Toll
     */
    @Deprecated
    private long toll;

    private long expire;

    /**
     * {@inheritDoc}
     */
    @Override
    public void readImpl() {
        accountId = readD();
        result = readC() == 1;

        if (result) {
            accountName = readS();
            accountTime = new AccountTime();

            accountTime.setAccumulatedOnlineTime(readQ());
            accountTime.setAccumulatedRestTime(readQ());
            accountTime.setAuthTime(new Timestamp(readQ()));
            accountTime.setPrevAuthTime(new Timestamp(readQ()));

            accessLevel = (byte) readC();
            membership = (byte) readC();
            toll = readQ();
            expire = readQ();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runImpl() {
        LoginServer.getInstance().accountAuthenticationResponse(accountId, accountName, result, accountTime, accessLevel, membership, toll, expire);
    }
}
