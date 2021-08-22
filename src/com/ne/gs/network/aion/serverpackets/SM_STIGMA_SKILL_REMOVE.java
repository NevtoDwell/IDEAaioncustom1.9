/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

public class SM_STIGMA_SKILL_REMOVE extends AionServerPacket {

    private final int skillId;

    public SM_STIGMA_SKILL_REMOVE(int skillId) {
        this.skillId = skillId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeH(skillId);
        writeC(1);
        writeC(1);
    }
}
