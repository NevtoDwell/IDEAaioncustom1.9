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
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import javolution.util.FastMap;

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.SkillUse;

/**
 * @author vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillUseData")
public class SkillUseData extends XMLQuest {

    @XmlAttribute(name = "start_npc_id")
    protected int startNpc;
    @XmlAttribute(name = "end_npc_id")
    protected int endNpc;
    @XmlElement(name = "skill", required = true)
    protected List<QuestSkillData> skills;

    @Override
    public void register(QuestEngine questEngine) {
        FastMap<Integer, QuestSkillData> questSkills = new FastMap<>();
        for (QuestSkillData qsd : skills) {
            questSkills.put(qsd.getSkillId(), qsd);
        }
        SkillUse questTemplate = new SkillUse(id, startNpc, endNpc, questSkills);
        questEngine.addQuestHandler(questTemplate);
    }
}
