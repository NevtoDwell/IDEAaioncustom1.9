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
 * Request to create
 *
 * @author SoulKeeper
 */
public class CM_MACRO_CREATE extends AionClientPacket {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CM_MACRO_CREATE.class);

    /**
     * Macro number. Fist is 1, second is 2. Starting from 1, not from 0
     */
    private int macroPosition;

    /**
     * XML that represents the macro
     */
    private String macroXML;

    /**
     * Read macro data
     */
    @Override
    protected void readImpl() {
        macroPosition = readC();
        macroXML = readS();
    }

    /**
     * Logging
     */
    @Override
    protected void runImpl() {
        log.debug(String.format("Created Macro #%d: %s", macroPosition, macroXML));

        PlayerService.addMacro(getConnection().getActivePlayer(), macroPosition, macroXML);

        sendPacket(SM_MACRO_RESULT.SM_MACRO_CREATED);
    }
}
