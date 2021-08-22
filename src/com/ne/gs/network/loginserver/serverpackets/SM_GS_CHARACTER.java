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
 * @author cura
 */
public class SM_GS_CHARACTER extends LsServerPacket {

    private final int accountId;
    private final int characterCount;

    /**
     * @param accountId
     * @param characterCount
     */
    public SM_GS_CHARACTER(int accountId, int characterCount) {
        super(0x08);
        this.accountId = accountId;
        this.characterCount = characterCount;
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeD(accountId);
        writeC(characterCount);
    }
}
