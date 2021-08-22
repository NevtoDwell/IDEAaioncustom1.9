/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.panels;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillPanel")
public class SkillPanel {

    @XmlAttribute(name = "panel_id")
    protected byte id;

    @XmlAttribute(name = "panel_skills")
    protected List<Integer> skills;

    public int getPanelId() {
        return id;
    }

    public List<Integer> getSkills() {
        return null;
    }

    public boolean canUseSkill(int skillId, int level) {
        for (Integer skill : skills) {
            if (skill >> 8 == skillId && (skill & 0xFF) == level) {
                return true;
            }
        }
        return false;
    }
}
