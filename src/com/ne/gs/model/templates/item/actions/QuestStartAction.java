/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * @author Nemiroff Date: 17.12.2009
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestStartAction")
public class QuestStartAction extends AbstractItemAction {

    @XmlAttribute
    protected int questid;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        QuestState qs = player.getQuestStateList().getQuestState(questid);
        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            return true;
        }
        if (qs.getStatus() != QuestStatus.COMPLETE) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_WORKING_QUEST);
        } else if (!qs.canRepeat()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_NONE_REPEATABLE(DataManager.QUEST_DATA.getQuestById(questid).getName()));
        }
        return false;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {

        QuestEngine.getInstance().onDialog(new QuestEnv(null, player, questid, QuestDialog.ASK_ACCEPTION.id()));
    }
}
