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

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.RelicRewards;

/**
 * @author Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelicRewardsData")

public class RelicRewardsData extends XMLQuest {

    @XmlAttribute(name = "start_npc_ids", required = true)
    protected List<Integer> startNpcIds;
    @XmlAttribute(name = "relic_var1")
    protected int relicVar1;
    @XmlAttribute(name = "relic_var2")
    protected int relicVar2;
    @XmlAttribute(name = "relic_var3")
    protected int relicVar3;
    @XmlAttribute(name = "relic_var4")
    protected int relicVar4;
    @XmlAttribute(name = "relic_count")
    protected int relicCount;

    @Override
    public void register(QuestEngine questEngine) {
        RelicRewards template = new RelicRewards(id, startNpcIds, relicVar1, relicVar2, relicVar3, relicVar4, relicCount);
        questEngine.addQuestHandler(template);
    }

}
