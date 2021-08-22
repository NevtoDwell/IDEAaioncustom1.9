/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.conditions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestStatusCondition")
public class QuestStatusCondition extends QuestCondition {

    @XmlAttribute(required = true)
    protected QuestStatus value;
    @XmlAttribute(name = "quest_id")
    protected Integer questId;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.template.xmlQuest.condition.QuestCondition#doCheck(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public boolean doCheck(QuestEnv env) {
        Player player = env.getPlayer();
        int qstatus = 0;
        int id = env.getQuestId();
        if (questId != null) {
            id = questId;
        }
        QuestState qs = player.getQuestStateList().getQuestState(id);
        if (qs != null) {
            qstatus = qs.getStatus().value();
        }

        switch (getOp()) {
            case EQUAL:
                return qstatus == value.value();
            case GREATER:
                return qstatus > value.value();
            case GREATER_EQUAL:
                return qstatus >= value.value();
            case LESSER:
                return qstatus < value.value();
            case LESSER_EQUAL:
                return qstatus <= value.value();
            case NOT_EQUAL:
                return qstatus != value.value();
            default:
                return false;
        }
    }
}
