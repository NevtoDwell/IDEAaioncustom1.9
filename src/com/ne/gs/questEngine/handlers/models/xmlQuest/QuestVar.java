/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestVar", propOrder = {"npc"})
public class QuestVar {

    protected List<QuestNpc> npc;
    @XmlAttribute(required = true)
    protected int value;

    public boolean operate(QuestEnv env, QuestState qs) {
        int var = -1;
        if (qs != null) {
            var = qs.getQuestVars().getQuestVars();
        }
        if (var != value) {
            return false;
        }
        for (QuestNpc questNpc : npc) {
            if (questNpc.operate(env, qs)) {
                return true;
            }
        }
        return false;
    }
}
