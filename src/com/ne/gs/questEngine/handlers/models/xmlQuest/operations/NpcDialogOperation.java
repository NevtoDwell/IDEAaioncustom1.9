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

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcDialogOperation")
public class NpcDialogOperation extends QuestOperation {

    @XmlAttribute(required = true)
    protected int id;
    @XmlAttribute(name = "quest_id")
    protected Integer questId;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperation#doOperate(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public void doOperate(QuestEnv env) {
        Player player = env.getPlayer();
        VisibleObject obj = env.getVisibleObject();
        int qId = env.getQuestId();
        if (questId != null) {
            qId = questId;
        }
        if (qId == 0) {
            player.sendPck(new SM_DIALOG_WINDOW(obj.getObjectId(), id));
        } else {
            player.sendPck(new SM_DIALOG_WINDOW(obj.getObjectId(), id, qId));
        }
    }

}
