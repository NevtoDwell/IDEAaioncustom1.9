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
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.skillengine.model.SkillLearnTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "skill_tree")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillTreeData {

    @XmlElement(name = "skill")
    private List<SkillLearnTemplate> skillTemplates;

    private final TIntObjectHashMap<ArrayList<SkillLearnTemplate>> templates = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<ArrayList<SkillLearnTemplate>> templatesById = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (SkillLearnTemplate template : skillTemplates) {
            addTemplate(template);
        }
        skillTemplates = null;
    }

    private void addTemplate(SkillLearnTemplate template) {
        Race race = template.getRace();
        if (race == null) {
            race = Race.PC_ALL;
        }

        int hash = makeHash(template.getClassId().ordinal(), race.ordinal(), template.getMinLevel());
        ArrayList<SkillLearnTemplate> value = templates.get(hash);
        if (value == null) {
            value = new ArrayList<>();
            templates.put(hash, value);
        }

        value.add(template);

        value = templatesById.get(template.getSkillId());
        if (value == null) {
            value = new ArrayList<>();
            templatesById.put(template.getSkillId(), value);
        }

        value.add(template);
    }

    /**
     * @return the templates
     */
    public TIntObjectHashMap<ArrayList<SkillLearnTemplate>> getTemplates() {
        return templates;
    }

    /**
     * Perform search for: - class specific skills (race = ALL) - class and race specific skills - non-specific skills
     * (race = ALL, class = ALL)
     *
     * @param playerClass
     * @param level
     * @param race
     *
     * @return SkillLearnTemplate[]
     */
    public SkillLearnTemplate[] getTemplatesFor(PlayerClass playerClass, int level, Race race) {
        List<SkillLearnTemplate> newSkills = new ArrayList<>();

        List<SkillLearnTemplate> classRaceSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), race.ordinal(),
            level));
        List<SkillLearnTemplate> classSpecificTemplates = templates.get(makeHash(playerClass.ordinal(),
            Race.PC_ALL.ordinal(), level));
        List<SkillLearnTemplate> generalTemplates = templates.get(makeHash(PlayerClass.ALL.ordinal(),
            Race.PC_ALL.ordinal(), level));

        if (classRaceSpecificTemplates != null) {
            newSkills.addAll(classRaceSpecificTemplates);
        }
        if (classSpecificTemplates != null) {
            newSkills.addAll(classSpecificTemplates);
        }
        if (generalTemplates != null) {
            newSkills.addAll(generalTemplates);
        }

        return newSkills.toArray(new SkillLearnTemplate[newSkills.size()]);
    }

    public SkillLearnTemplate[] getTemplatesForSkill(int skillId) {
        List<SkillLearnTemplate> searchSkills = new ArrayList<>();

        List<SkillLearnTemplate> byId = templatesById.get(skillId);
        if (byId != null) {
            searchSkills.addAll(byId);
        }

        return searchSkills.toArray(new SkillLearnTemplate[searchSkills.size()]);
    }

    public boolean isLearnedSkill(int skillId) {
        return templatesById.get(skillId) != null;
    }

    public int size() {
        int size = 0;
        for (Integer key : templates.keys()) {
            size += templates.get(key).size();
        }
        return size;
    }

    private static int makeHash(int classId, int race, int level) {
        int result = classId << 8;
        result = (result | race) << 8;
        return result | level;
    }
}
