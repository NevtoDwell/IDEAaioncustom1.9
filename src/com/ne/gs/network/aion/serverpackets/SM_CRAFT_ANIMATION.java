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
 * @author Mr. Poke
 */
public class SM_CRAFT_ANIMATION extends AionServerPacket {

    private final int senderObjectId;
    private final int targetObjectId;
    private final int skillId;
    private final int action;

    /**
     * @param senderObjectId
     * @param targetObjectId
     * @param skillId
     * @param action
     */
    public SM_CRAFT_ANIMATION(int senderObjectId, int targetObjectId, int skillId, int action) {
        this.senderObjectId = senderObjectId;
        this.targetObjectId = targetObjectId;
        this.skillId = skillId;
        this.action = action;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(senderObjectId);
        writeD(targetObjectId);
        writeH(skillId);
        writeC(action);
    }
}
