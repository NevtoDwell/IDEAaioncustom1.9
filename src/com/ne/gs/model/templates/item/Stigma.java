/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Stigma")
public class Stigma {

    @XmlElement(name = "require_skill")
    protected List<RequireSkill> requireSkill;
    @XmlAttribute
    protected List<String> skill;
    @XmlAttribute
    protected int shard = 1;

    /**
     * @return the skillid
     */
    public List<StigmaSkill> getSkills() {
        List<StigmaSkill> list = new ArrayList<>();
        for (String st : skill) {
            String[] array = st.split(":");
            list.add(new StigmaSkill(Integer.parseInt(array[0]), Integer.parseInt(array[1])));
        }

        return list;
    }

    /**
     * @return the shard
     */
    public int getShard() {
        return shard;
    }

    public List<RequireSkill> getRequireSkill() {
        if (requireSkill == null) {
            requireSkill = new ArrayList<>();
        }
        return this.requireSkill;
    }

    public class StigmaSkill {

        private final int skillId;
        private final int skillLvl;

        public StigmaSkill(int skillLvl, int skillId) {
            this.skillId = skillId;
            this.skillLvl = skillLvl;
        }

        public int getSkillLvl() {
            return skillLvl;
        }

        public int getSkillId() {
            return skillId;
        }
    }
}
