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
import com.ne.gs.network.aion.serverpackets.SM_ACCOUNT_PROPERTIES;
import com.ne.gs.network.aion.serverpackets.SM_CHARACTER_LIST;

/**
 * In this packets aion client is requesting character list.
 *
 * @author -Nemesiss-
 */
public class CM_CHARACTER_LIST extends AionClientPacket {

    /**
     * PlayOk2 - we dont care...
     */
    private int playOk2;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        playOk2 = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        sendPacket(new SM_CHARACTER_LIST(playOk2));
        if (getConnection().getAccount().getAccessLevel() > 3) {
            sendPacket(new SM_ACCOUNT_PROPERTIES());
        }
    }
}
