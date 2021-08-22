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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import javolution.util.FastMap;

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.KillSpawned;

/**
 * @author vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillSpawnedData")
public class KillSpawnedData extends MonsterHuntData {

    @XmlElement(name = "spawned_monster", required = true)
    protected List<SpawnedMonster> spawnedMonster;

    @Override
    public void register(QuestEngine questEngine) {
        FastMap<List<Integer>, SpawnedMonster> spawnedMonsters = new FastMap<>();
        for (SpawnedMonster m : spawnedMonster) {
            spawnedMonsters.put(m.getNpcIds(), m);
        }
        KillSpawned template = new KillSpawned(id, startNpcIds, endNpcIds, spawnedMonsters);
        questEngine.addQuestHandler(template);
    }
}
