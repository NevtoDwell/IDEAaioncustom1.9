/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.clientpackets;

import com.ne.gs.network.BannedMacManager;
import com.ne.gs.network.loginserver.LsClientPacket;

/**
 * @author KID
 */
public class CM_MACBAN_LIST extends LsClientPacket {

    public CM_MACBAN_LIST(int opCode) {
        super(opCode);
    }

    @Override
    protected void readImpl() {
        BannedMacManager bmm = BannedMacManager.getInstance();
        int cnt = readD();
        for (int a = 0; a < cnt; a++) {
            bmm.dbLoad(readS(), readQ(), readS());
        }

        bmm.onEnd();
    }

    @Override
    protected void runImpl() {
        // ?
    }
}
