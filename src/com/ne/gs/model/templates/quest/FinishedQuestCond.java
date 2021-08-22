/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FinishedQuest", propOrder = {"questId", "reward"})
public class FinishedQuestCond {

    @XmlAttribute(name = "quest_id", required = true)
    protected int questId;
    @XmlAttribute(name = "reward")
    protected int reward;

    public int getQuestId() {
        return questId;
    }

    public int getReward() {
        return reward;
    }
}
