/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.serverpackets.SM_CREATE_CHARACTER;
import com.ne.gs.network.aion.serverpackets.SM_NICKNAME_CHECK_RESPONSE;
import com.ne.gs.services.NameRestrictionService;
import com.ne.gs.services.player.PlayerService;

/**
 * In this packets aion client is asking if given nickname is ok/free?.
 *
 * @author -Nemesiss-
 * @modified cura
 */
public class CM_CHECK_NICKNAME extends AionClientPacket {

    /**
     * nick name that need to be checked
     */
    private String nick;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        nick = readS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection client = getConnection();

        if (!PlayerService.isFreeName(nick) || PlayerService.isOldName(nick)) {
            if (GSConfig.CHARACTER_CREATION_MODE == 2) {
                client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(SM_CREATE_CHARACTER.RESPONSE_NAME_RESERVED));
            } else {
                client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(SM_CREATE_CHARACTER.RESPONSE_NAME_ALREADY_USED));
            }
        } else if (!NameRestrictionService.isValidName(nick)) {
            client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(SM_CREATE_CHARACTER.RESPONSE_INVALID_NAME));
        } else if (NameRestrictionService.isForbiddenWord(nick)) {
            client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_FORBIDDEN_CHAR_NAME));
        } else {
            client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(0));
        }
    }
}
