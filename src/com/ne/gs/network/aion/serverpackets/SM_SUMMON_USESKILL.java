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
 * @author ATracer
 */
public class SM_SUMMON_USESKILL extends AionServerPacket {

    private final int summonId;
    private final int skillId;
    private final int skillLvl;
    private final int targetId;

    /**
     * @param summonId
     * @param skillId
     * @param skillLvl
     * @param targetId
     */
    public SM_SUMMON_USESKILL(int summonId, int skillId, int skillLvl, int targetId) {
        this.summonId = summonId;
        this.skillId = skillId;
        this.skillLvl = skillLvl;
        this.targetId = targetId;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(summonId);
        writeH(skillId);
        writeC(skillLvl);
        writeD(targetId);
    }

}
