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
import com.ne.gs.network.aion.serverpackets.SM_MACRO_RESULT;
import com.ne.gs.services.player.PlayerService;

/**
 * Packet that is responsible for macro deletion.<br>
 * Client sends id in the macro list.<br>
 * For instance client has 4 macros and we are going to delete macro #3.<br>
 * Client sends request to delete macro #3.<br>
 * And macro #4 becomes macro #3.<br>
 * So we have to use a list to store macros properly.
 *
 * @author SoulKeeper
 */
public class CM_MACRO_DELETE extends AionClientPacket {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CM_MACRO_DELETE.class);

    /**
     * Macro id that has to be deleted
     */
    private int macroPosition;

    /**
     * Reading macro id
     */
    @Override
    protected void readImpl() {
        macroPosition = readC();
    }

    /**
     * Logging
     */
    @Override
    protected void runImpl() {
        log.debug("Request to delete macro #" + macroPosition);

        PlayerService.removeMacro(getConnection().getActivePlayer(), macroPosition);

        sendPacket(SM_MACRO_RESULT.SM_MACRO_DELETED);
    }
}
