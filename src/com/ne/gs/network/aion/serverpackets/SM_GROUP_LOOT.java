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
 * @author Rhys2002
 */
public class SM_GROUP_LOOT extends AionServerPacket {

    private final int groupId;
    private final int index;
    private final int unk2;
    private final int itemId;
    private final int unk3;
    private final int lootCorpseId;
    private final int distributionId;
    private final int playerId;
    private final long luck;

    /**
     */
    public SM_GROUP_LOOT(int groupId, int playerId, int itemId, int lootCorpseId, int distributionId, long luck,
                         int index) {
        this.groupId = groupId;
        this.index = index;
        unk2 = 1;
        this.itemId = itemId;
        unk3 = 0;
        this.lootCorpseId = lootCorpseId;
        this.distributionId = distributionId;
        this.playerId = playerId;
        this.luck = luck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(groupId);
        writeD(index);
        writeD(unk2);
        writeD(itemId);
        writeC(unk3);
        writeC(0);
        writeD(lootCorpseId);
        writeC(distributionId);
        writeD(playerId);
        writeD((int) luck);
    }
}
