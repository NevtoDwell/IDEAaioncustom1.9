/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.account.Account;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_RESTORE_CHARACTER;
import com.ne.gs.services.player.PlayerService;

/**
 * In this packets aion client is requesting cancellation of character deleting.
 *
 * @author -Nemesiss-
 */
public class CM_RESTORE_CHARACTER extends AionClientPacket {

    /**
     * PlayOk2 - we dont care...
     */
    @SuppressWarnings("unused")
    private int playOk2;
    /**
     * ObjectId of character that deletion should be canceled
     */
    private int chaOid;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        playOk2 = readD();
        chaOid = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Account account = getConnection().getAccount();
        PlayerAccountData pad = account.getPlayerAccountData(chaOid);

        boolean success = pad != null && PlayerService.cancelPlayerDeletion(pad);
        sendPacket(new SM_RESTORE_CHARACTER(chaOid, success));
    }
}
