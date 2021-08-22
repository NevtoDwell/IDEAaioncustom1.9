/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import javolution.util.FastMap;

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.MentorMonsterHunt;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MentorMonsterHuntData")
public class MentorMonsterHuntData extends MonsterHuntData {

    @XmlAttribute(name = "min_mente_level")
    protected int minMenteLevel = 1;
    @XmlAttribute(name = "max_mente_level")
    protected int maxMenteLevel = 99;

    public int getMinMenteLevel() {
        return minMenteLevel;
    }

    public int getMaxMenteLevel() {
        return maxMenteLevel;
    }

    @Override
    public void register(QuestEngine questEngine) {
        FastMap<List<Integer>, Monster> monsterNpcs = new FastMap<>();
        for (Monster m : monster) {
            monsterNpcs.put(m.getNpcIds(), m);
        }
        MentorMonsterHunt template = new MentorMonsterHunt(id, startNpcIds, endNpcIds, aggroStartNpcsIds, monsterNpcs, minMenteLevel, maxMenteLevel);

        questEngine.addQuestHandler(template);
    }
}
