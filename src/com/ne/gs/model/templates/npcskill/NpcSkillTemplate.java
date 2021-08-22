/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.npcskill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionChs Master
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "npcskill")
public class NpcSkillTemplate {

    @XmlAttribute(name = "id")
    protected int id;
    @XmlAttribute(name = "skillid")
    protected int skillid;
    @XmlAttribute(name = "skilllevel")
    protected int skilllevel;
    @XmlAttribute(name = "probability")
    protected int probability;

    @XmlAttribute(name = "minhp")
    protected int minhp;
    @XmlAttribute(name = "maxhp")
    protected int maxhp;

    @XmlAttribute(name = "maxtime")
    protected int maxtime = 0;

    @XmlAttribute(name = "mintime")
    protected int mintime = 0;

    @XmlAttribute(name = "conjunction")
    protected ConjunctionType conjunction = ConjunctionType.AND;

    @XmlAttribute(name = "cooldown")
    protected int cooldown = 0;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the skillid
     */
    public int getSkillid() {
        return skillid;
    }

    /**
     * @return the skilllevel
     */
    public int getSkillLevel() {
        return skilllevel;
    }

    /**
     * @return the probability
     */
    public int getProbability() {
        return probability;
    }

    /**
     * @return the minhp
     */
    public int getMinhp() {
        return minhp;
    }

    /**
     * @return the maxhp
     */
    public int getMaxhp() {
        return maxhp;
    }

    public int getMinTime() {
        return mintime;
    }

    public int getMaxTime() {
        return maxtime;
    }

    public ConjunctionType getConjunctionType() {
        return conjunction;
    }

    public int getCooldown() {
        return cooldown;
    }
}
