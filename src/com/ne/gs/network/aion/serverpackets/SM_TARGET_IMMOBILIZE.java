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
 * @author Sweetkr
 */
public class SM_TARGET_IMMOBILIZE extends AionServerPacket {

    private final Creature creature;

    public SM_TARGET_IMMOBILIZE(Creature creature) {
        this.creature = creature;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(creature.getObjectId());
        writeF(creature.getX());
        writeF(creature.getY());
        writeF(creature.getZ());
        writeC(creature.getHeading());
    }
}
