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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "skill_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillData {

    @XmlElement(name = "skill_template")
    private List<SkillTemplate> skillTemplates;
    private HashMap<Integer, ArrayList<Integer>> cooldownGroups;
    /**
     * Map that contains skillId - SkillTemplate key-value pair
     */
    private final TIntObjectHashMap<SkillTemplate> skillData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {

        skillData.clear();
        for (SkillTemplate skillTempalte : skillTemplates) {
            skillData.put(skillTempalte.getSkillId(), skillTempalte);
        }

        initializeCooldownGroups();

        skillTemplates = null;
    }

    /**
     * @param skillId
     *
     * @return SkillTemplate
     */
    public SkillTemplate getSkillTemplate(int skillId) {
        return skillData.get(skillId);
    }

    /**
     * @return skillData.size()
     */
    public int size() {
        return skillData.size();
    }

    /**
     * @return the skillTemplates
     */
    public Collection<SkillTemplate> getSkillTemplates() {
        return skillData.valueCollection();
    }

    /**
     * @param skillTemplates
     *     the skillTemplates to set
     */
    public void setSkillTemplates(List<SkillTemplate> skillTemplates) {
        this.skillTemplates = skillTemplates;
        afterUnmarshal(null, null);
    }

    /**
     * This method creates a HashMap with all skills assigned to their representative cooldownIds
     */
    private void initializeCooldownGroups() {
        cooldownGroups = new HashMap<>();
        for (SkillTemplate skillTemplate : skillTemplates) {
            int cooldownId = skillTemplate.getCooldownId();
            if (!cooldownGroups.containsKey(cooldownId)) {
                cooldownGroups.put(cooldownId, new ArrayList<Integer>());
            }
            cooldownGroups.get(cooldownId).add(skillTemplate.getSkillId());
        }
    }

    /**
     * This method is used to get all skills assigned to a specific cooldownId
     *
     * @param cooldownId
     *
     * @return ArrayList<Integer> including all skills for asked cooldownId
     */
    public ArrayList<Integer> getSkillsForCooldownId(int cooldownId) {
        return cooldownGroups.get(cooldownId);
    }
}
