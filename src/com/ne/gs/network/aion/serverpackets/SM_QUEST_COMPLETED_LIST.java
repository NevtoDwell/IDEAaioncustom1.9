/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import javolution.util.FastList;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.QuestsData;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.questEngine.model.QuestState;

/**
 * @author MrPoke
 */
public class SM_QUEST_COMPLETED_LIST extends AionServerPacket {

    private FastList<QuestState> questState;

    public SM_QUEST_COMPLETED_LIST(FastList<QuestState> questState) {
        this.questState = questState;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(0x01); // 2.1
        writeH(-questState.size() & 0xFFFF);
        QuestsData QUEST_DATA = DataManager.QUEST_DATA;
        for (QuestState qs : questState) {
            writeH(qs.getQuestId());
            writeH(QUEST_DATA.getQuestById(qs.getQuestId()).getCategory().getId());
            writeC(qs.getCompleteCount());
        }
        FastList.recycle(questState);
        questState = null;
    }
}
