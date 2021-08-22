/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import javolution.util.FastMap;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author MrPoke
 */

public class SM_NEARBY_QUESTS extends AionServerPacket {

    private FastMap<Integer, Integer> questIds;

    public SM_NEARBY_QUESTS(FastMap<Integer, Integer> questIds) {
        this.questIds = questIds;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        if (questIds == null || con.getActivePlayer() == null) {
            return;
        }

        writeC(0);
        writeH(-questIds.size() & 0xFFFF);
        for (FastMap.Entry<Integer, Integer> e = questIds.head(), end = questIds.tail(); (e = e.getNext()) != end; ) {
            writeH(e.getKey());
            writeH(e.getValue());
        }
        FastMap.recycle(questIds);
        questIds = null;
    }
}
