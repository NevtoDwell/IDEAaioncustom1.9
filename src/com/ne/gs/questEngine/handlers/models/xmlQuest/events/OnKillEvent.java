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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.questEngine.handlers.models.Monster;
import com.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperations;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnKillEvent", propOrder = {"monster", "complite"})
public class OnKillEvent extends QuestEvent {

    @XmlElement(name = "monster")
    protected List<Monster> monster;
    protected QuestOperations complite;

    public List<Monster> getMonsters() {
        if (monster == null) {
            monster = new ArrayList<>();
        }
        return this.monster;
    }

    public boolean operate(QuestEnv env) {
        if (monster == null || !(env.getVisibleObject() instanceof Npc)) {
            return false;
        }

        QuestState qs = env.getPlayer().getQuestStateList().getQuestState(env.getQuestId());
        if (qs == null) {
            return false;
        }

        Npc npc = (Npc) env.getVisibleObject();
        for (Monster m : monster) {
            if (m.getNpcIds().contains(npc.getNpcId())) {
                int var = qs.getQuestVarById(m.getVar());
                if (var >= (m.getStartVar() == null ? 0 : m.getStartVar()) && var < m.getEndVar()) {
                    qs.setQuestVarById(m.getVar(), var + 1);
                    env.getPlayer().sendPck(new SM_QUEST_ACTION(env.getQuestId(), qs.getStatus(), qs
                        .getQuestVars().getQuestVars()));
                }
            }
        }

        if (complite != null) {
            for (Monster m : monster) {
                if (qs.getQuestVarById(m.getVar()) != qs.getQuestVarById(m.getVar())) {
                    return false;
                }
            }
            complite.operate(env);
        }
        return false;
    }
}
