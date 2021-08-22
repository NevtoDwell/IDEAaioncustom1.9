/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_INFO;

/**
 * @author Lyahim
 */
public class CM_INSTANCE_INFO extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_INSTANCE_INFO.class);

    private int unk1, unk2;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        unk1 = readD();
        unk2 = readC(); // channel?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        if (unk2 == 1 && !getConnection().getActivePlayer().isInTeam()) {
            log.debug("Received CM_INSTANCE_INFO with teamdata request but player has no team!");
        }
        sendPacket(new SM_INSTANCE_INFO(getConnection().getActivePlayer(), true, getConnection().getActivePlayer().getCurrentTeam()));
    }
}
