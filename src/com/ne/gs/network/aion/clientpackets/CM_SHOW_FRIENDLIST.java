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
import com.ne.gs.network.aion.serverpackets.SM_FRIEND_LIST;

/**
 * Send when the client requests the friendlist
 *
 * @author Ben
 */
public class CM_SHOW_FRIENDLIST extends AionClientPacket {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        sendPacket(new SM_FRIEND_LIST());

    }

}
