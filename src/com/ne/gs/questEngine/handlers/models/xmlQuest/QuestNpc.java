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

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestNpc", propOrder = {"dialog"})
public class QuestNpc {

    protected List<QuestDialog> dialog;
    @XmlAttribute(required = true)
    protected int id;

    public boolean operate(QuestEnv env, QuestState qs) {
        int npcId = -1;
        if (env.getVisibleObject() instanceof Npc) {
            npcId = ((Npc) env.getVisibleObject()).getNpcId();
        }
        if (npcId != id) {
            return false;
        }
        for (QuestDialog questDialog : dialog) {
            if (questDialog.operate(env, qs)) {
                return true;
            }
        }
        return false;
    }
}
