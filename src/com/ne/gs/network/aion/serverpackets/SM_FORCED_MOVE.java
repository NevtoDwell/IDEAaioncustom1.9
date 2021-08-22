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
public class SM_FORCED_MOVE extends AionServerPacket {

    private final Creature creature;
    private final int objectId;
    private final float x;
    private final float y;
    private final float z;

    public SM_FORCED_MOVE(Creature creature, Creature target) {
        this(creature, target.getObjectId(), target.getX(), target.getY(), target.getZ());
    }

    public SM_FORCED_MOVE(Creature creature, int objectId, float x, float y, float z) {
        this.creature = creature;
        this.objectId = objectId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(creature.getObjectId());
        writeD(objectId);// targets objectId
        writeC(16); // unk
        writeF(x);
        writeF(y);
        writeF(z);
    }
}
