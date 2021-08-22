/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * Response to SM_QUESTION_WINDOW
 *
 * @author Ben
 * @author Sarynth
 */
public class CM_QUESTION_RESPONSE extends AionClientPacket {

    private int questionid;
    private int response;
    @SuppressWarnings("unused")
    private int senderid;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        questionid = readD();

        response = readC(); // y/n
        readC(); // unk 0x00 - 0x01 ?
        readH();
        senderid = readD();
        readD();
        readH();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player.isTrading()) {
            return;
        }
        player.getResponseRequester().respond(questionid, response);
    }

}
