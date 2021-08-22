/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SetQuestStatusOperation")
public class SetQuestStatusOperation extends QuestOperation {

    @XmlAttribute(required = true)
    protected QuestStatus status;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperation#doOperate(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public void doOperate(QuestEnv env) {
        Player player = env.getPlayer();
        int questId = env.getQuestId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null) {
            qs.setStatus(status);
            player.sendPck(new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars()
                .getQuestVars()));
            if (qs.getStatus() == QuestStatus.COMPLETE) {
                player.getController().updateNearbyQuests();
            }
        }
    }
}
