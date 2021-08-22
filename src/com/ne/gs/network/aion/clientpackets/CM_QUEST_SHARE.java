/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.QuestService;
import com.ne.gs.utils.MathUtil;

public class CM_QUEST_SHARE extends AionClientPacket {

    public int questId;

    @Override
    protected void readImpl() {
        questId = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (player == null) {
            return;
        }
        if (!player.isInGroup2()) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1100000));
            return;
        }

        QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(questId);

        if ((questTemplate == null) || (questTemplate.isCannotShare())) {
            return;
        }
        QuestState questState = player.getQuestStateList().getQuestState(questId);

        if ((questState == null) || (questState.getStatus() == QuestStatus.COMPLETE)) {
            return;
        }
        for (Player member : player.getPlayerGroup2().getOnlineMembers()) {
            if ((player != member) && (MathUtil.isIn3dRange(member, player, GroupConfig.GROUP_MAX_DISTANCE)) && (!member.getQuestStateList().hasQuest(questId))) {
                if (!QuestService.checkLevelRequirement(questId, member.getLevel())) {
                    player.sendPck(new SM_SYSTEM_MESSAGE(1100003, member.getName()));
                    member.sendPck(new SM_SYSTEM_MESSAGE(1100003, player.getName()));
                } else {
                    member.sendPck(new SM_QUEST_ACTION(questId, member.getObjectId(), true));
                }
            }
        }
    }
}
