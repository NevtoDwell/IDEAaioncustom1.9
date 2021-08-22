/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.chatserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.network.chatserver.CsClientPacket;
import com.ne.gs.services.ChatService;

/**
 * @author ATracer
 */
public class CM_CS_PLAYER_AUTH_RESPONSE extends CsClientPacket {

    protected static final Logger log = LoggerFactory.getLogger(CM_CS_PLAYER_AUTH_RESPONSE.class);

    /**
     * Player for which authentication was performed
     */
    private int playerId;
    /**
     * Token will be sent to client
     */
    private byte[] token;

    /**
     * @param opcode
     */
    public CM_CS_PLAYER_AUTH_RESPONSE(int opcode) {
        super(opcode);
    }

    @Override
    protected void readImpl() {
        playerId = readD();
        int tokenLenght = readC();
        token = readB(tokenLenght);
    }

    @Override
    protected void runImpl() {
        ChatService.playerAuthed(playerId, token);
    }
}
