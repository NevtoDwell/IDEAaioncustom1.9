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
 * @author Sweetkr
 */
public class SM_SKILL_ACTIVATION extends AionServerPacket {

    private final boolean isActive;
    private final int unk;
    private final int skillId;

    /**
     * For toggle skills
     *
     * @param skillId
     * @param isActive
     */
    public SM_SKILL_ACTIVATION(int skillId, boolean isActive) {
        this.skillId = skillId;
        this.isActive = isActive;
        unk = 0;
    }

    /**
     * For stigma remove should work in 1.5.1.15
     *
     * @param skillId
     */
    public SM_SKILL_ACTIVATION(int skillId) {
        this.skillId = skillId;
        isActive = true;
        unk = 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeH(skillId);
        writeD(unk);
        writeC(isActive ? 1 : 0);
    }
}
