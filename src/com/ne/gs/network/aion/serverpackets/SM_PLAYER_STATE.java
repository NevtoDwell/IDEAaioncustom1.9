/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * So far I've found only one usage for this packet - to stop character blinking ( just after login into game, player's character is blinking )
 *
 * @author Luno, Sweetkr states: 0 - normal char 1- crounched invisible char 64 - standing blinking char 128- char is invisible
 */
public class SM_PLAYER_STATE extends AionServerPacket {

    private final int playerObjId;
    private final int visualState;
    private final int seeState;

    public SM_PLAYER_STATE(Creature creature) {
        playerObjId = creature.getObjectId();
        visualState = creature.getVisualState();
        seeState = creature.getSeeState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjId);
        writeC(visualState);
        writeC(seeState);
        writeC(visualState == 64 ? 0x01 : 0x00);
    }
}
