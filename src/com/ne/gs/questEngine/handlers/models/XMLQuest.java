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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.questEngine.QuestEngine;

/**
 * @author MrPoke, Hilgert
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestScriptData")
@XmlSeeAlso({ReportToData.class, RelicRewardsData.class, CraftingRewardsData.class, ReportToManyData.class, MonsterHuntData.class, ItemCollectingData.class, WorkOrdersData.class, XmlQuestData.class,
             MentorMonsterHuntData.class, ItemOrdersData.class, FountainRewardsData.class})
public abstract class XMLQuest {

    @XmlAttribute(name = "id", required = true)
    protected int id;
    @XmlAttribute(name = "movie", required = false)
    protected int questMovie;

    /**
     * Gets the value of the id property.
     */
    public int getId() {
        return id;
    }

    public int getQuestMovie() {
        return questMovie;
    }

    public abstract void register(QuestEngine questEngine);
}
