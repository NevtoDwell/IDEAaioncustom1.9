/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.templates.npcskill.NpcSkillTemplates;

/**
 * @author ATracer
 */
@XmlRootElement(name = "npc_skill_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcSkillData {

    @XmlElement(name = "npcskills")
    private List<NpcSkillTemplates> npcSkills;

    /**
     * A map containing all npc skill templates
     */
    private final TIntObjectHashMap<NpcSkillTemplates> npcSkillData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (NpcSkillTemplates npcSkill : npcSkills) {
            npcSkillData.put(npcSkill.getNpcId(), npcSkill);

            if (npcSkill.getNpcSkills() == null) {
                LoggerFactory.getLogger(NpcSkillData.class).error("NO SKILL");
            }
        }
        npcSkills = null;
    }

    public int size() {
        return npcSkillData.size();
    }

    public NpcSkillTemplates getNpcSkillList(int id) {
        return npcSkillData.get(id);
    }
}
