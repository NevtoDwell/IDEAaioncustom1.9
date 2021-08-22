/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.services.QuestService;

public class CM_DELETE_QUEST extends AionClientPacket {

    public int questId;

    @Override
    protected void readImpl() {
        questId = readH();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        QuestTemplate qt = DataManager.QUEST_DATA.getQuestById(questId);

        if (qt != null && qt.isTimer()) {
            player.getController().cancelTask(TaskId.QUEST_TIMER);
            sendPacket(new SM_QUEST_ACTION(questId, 0));
        }
        if (!QuestService.abandonQuest(player, questId)) {
            return;
        }
        player.getController().updateNearbyQuests();
    }
}
