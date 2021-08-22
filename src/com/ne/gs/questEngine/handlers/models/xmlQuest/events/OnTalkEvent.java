/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.questEngine.handlers.models.xmlQuest.QuestVar;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnTalkEvent", propOrder = {"var"})
public class OnTalkEvent extends QuestEvent {

    protected List<QuestVar> var;

    public boolean operate(QuestEnv env) {
        if (conditions == null || conditions.checkConditionOfSet(env)) {
            QuestState qs = env.getPlayer().getQuestStateList().getQuestState(env.getQuestId());
            for (QuestVar questVar : var) {
                if (questVar.operate(env, qs)) {
                    return true;
                }
            }
        }
        return false;
    }
}
