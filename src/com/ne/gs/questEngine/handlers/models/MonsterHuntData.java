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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import javolution.util.FastMap;

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.MonsterHunt;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MonsterHuntData", propOrder = {"monster"})
@XmlSeeAlso({
    KillSpawnedData.class,
    MentorMonsterHuntData.class
})
public class MonsterHuntData extends XMLQuest {

    @XmlElement(name = "monster", required = true)
    protected List<Monster> monster;

    @XmlAttribute(name = "start_npc_ids", required = true)
    protected List<Integer> startNpcIds;

    @XmlAttribute(name = "aggro_start_npcs")
    protected List<Integer> aggroStartNpcsIds;

    @XmlAttribute(name = "end_npc_ids")
    protected List<Integer> endNpcIds;

    @Override
    public void register(QuestEngine questEngine) {
        FastMap<List<Integer>, Monster> monsterNpcs = new FastMap<>();
        for (Monster m : monster) {
            monsterNpcs.put(m.getNpcIds(), m);
        }
        MonsterHunt template = new MonsterHunt(id, startNpcIds, endNpcIds, aggroStartNpcsIds, monsterNpcs);
        questEngine.addQuestHandler(template);
    }

}
