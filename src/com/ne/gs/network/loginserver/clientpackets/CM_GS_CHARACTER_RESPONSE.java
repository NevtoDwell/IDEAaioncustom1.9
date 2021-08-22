/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.clientpackets;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.network.loginserver.LsClientPacket;
import com.ne.gs.network.loginserver.serverpackets.SM_GS_CHARACTER;

/**
 * @author cura
 */
public class CM_GS_CHARACTER_RESPONSE extends LsClientPacket {

    public CM_GS_CHARACTER_RESPONSE(int opCode) {
        super(opCode);
    }

    private int accountId;

    @Override
    public void readImpl() {
        accountId = readD();
    }

    @Override
    public void runImpl() {
        int characterCount = GDB.get(PlayerDAO.class).getCharacterCountOnAccount(accountId);
        sendPacket(new SM_GS_CHARACTER(accountId, characterCount));
    }
}
