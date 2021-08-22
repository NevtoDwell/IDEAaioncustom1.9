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
 * @author ATracer
 * @author Jego
 */
public class SM_RESURRECT extends AionServerPacket {

    private final String name;
    private final int skillId;

    public SM_RESURRECT(Creature creature) {
        this(creature, 0);
    }

    public SM_RESURRECT(Creature creature, int skillId) {
        name = creature.getName();
        this.skillId = skillId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeS(name);
        writeH(skillId); // unk
        writeD(0);
    }
}
