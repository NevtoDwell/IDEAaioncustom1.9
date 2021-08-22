/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import com.ne.gs.questEngine.handlers.models.XMLQuest;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "quest_scripts")
public class XMLQuests {

    @XmlElements({@XmlElement(name = "report_to", type = com.ne.gs.questEngine.handlers.models.ReportToData.class),
                  @XmlElement(name = "monster_hunt", type = com.ne.gs.questEngine.handlers.models.MonsterHuntData.class),
                  @XmlElement(name = "xml_quest", type = com.ne.gs.questEngine.handlers.models.XmlQuestData.class),
                  @XmlElement(name = "item_collecting", type = com.ne.gs.questEngine.handlers.models.ItemCollectingData.class),
                  @XmlElement(name = "relic_rewards", type = com.ne.gs.questEngine.handlers.models.RelicRewardsData.class),
                  @XmlElement(name = "crafting_rewards", type = com.ne.gs.questEngine.handlers.models.CraftingRewardsData.class),
                  @XmlElement(name = "report_to_many", type = com.ne.gs.questEngine.handlers.models.ReportToManyData.class),
                  @XmlElement(name = "kill_in_world", type = com.ne.gs.questEngine.handlers.models.KillInWorldData.class),
                  @XmlElement(name = "skill_use", type = com.ne.gs.questEngine.handlers.models.SkillUseData.class),
                  @XmlElement(name = "kill_spawned", type = com.ne.gs.questEngine.handlers.models.KillSpawnedData.class),
                  @XmlElement(name = "mentor_monster_hunt", type = com.ne.gs.questEngine.handlers.models.MentorMonsterHuntData.class),
                  @XmlElement(name = "fountain_rewards", type = com.ne.gs.questEngine.handlers.models.FountainRewardsData.class),
                  @XmlElement(name = "item_order", type = com.ne.gs.questEngine.handlers.models.ItemOrdersData.class),
                  @XmlElement(name = "work_order", type = com.ne.gs.questEngine.handlers.models.WorkOrdersData.class)})
    protected List<XMLQuest> data;

    /**
     * @return the data
     */
    public List<XMLQuest> getQuest() {
        return data;
    }

    /**
     * @param data
     *     the data to set
     */
    public void setData(List<XMLQuest> data) {
        this.data = data;
    }
}
