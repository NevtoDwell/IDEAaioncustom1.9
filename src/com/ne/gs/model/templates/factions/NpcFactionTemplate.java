/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.factions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.Race;


/**
 * @author vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcFaction")
public class NpcFactionTemplate {

    @XmlAttribute(name = "id", required = true)
    protected int id;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "nameId")
    protected int nameId;
    @XmlAttribute(name = "category")
    protected FactionCategory category;
    @XmlAttribute(name = "minlevel")
    protected Integer minlevel;
    @XmlAttribute(name = "maxlevel")
    protected int maxlevel = 99;
    @XmlAttribute(name = "race")
    protected Race race;
    @XmlAttribute(name = "npcid")
    protected int npcId;
    @XmlAttribute(name = "skill_points")
    protected int skillPoints;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNameId() {
        return nameId;
    }

    public FactionCategory getCategory() {
        return category;
    }

    public int getMinLevel() {
        return minlevel;
    }

    public int getMaxLevel() {
        return maxlevel;
    }

    public Race getRace() {
        return race;
    }

    public boolean isMentor() {
        return category == FactionCategory.MENTOR;
    }


    /**
     * @return the npcId
     */
    public int getNpcId() {
        return npcId;
    }

    public int getSkillPoints() {
        return skillPoints;
    }
}
