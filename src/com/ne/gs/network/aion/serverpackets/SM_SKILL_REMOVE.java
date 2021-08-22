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

/**
 * @author xTz
 */
public class SM_SKILL_REMOVE extends AionServerPacket {

    private final int skillId;
    private final int skillLevel;
    private final boolean isStigma;

    public SM_SKILL_REMOVE(int skillId, int skillLevel, boolean isStigma) {
        this.skillId = skillId;
        this.skillLevel = skillLevel;
        this.isStigma = isStigma;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeH(skillId);
        if (skillId >= 30001 && skillId <= 30003 || skillId >= 40001 && skillId <= 40010) {
            writeC(0);
            writeC(0);
        } else if (isStigma) {
            writeC(1);
            writeC(1);
        } else {
            writeC(skillLevel);
        }
    }
}
