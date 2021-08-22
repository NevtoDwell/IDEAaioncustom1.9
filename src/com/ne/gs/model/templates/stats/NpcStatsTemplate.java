/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Luno
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "npc_stats_template")
public class NpcStatsTemplate extends StatsTemplate {

    @XmlAttribute(name = "run_speed_fight")
    private float runSpeedFight;
    @XmlAttribute(name = "pdef")
    private int pdef;
    @XmlAttribute(name = "mdef")
    private int mdef;
    @XmlAttribute(name = "mresist")
    private int mresist;
    @XmlAttribute(name = "crit")
    private int crit;
    @XmlAttribute(name = "accuracy")
    private int accuracy;
    @XmlAttribute(name = "power")
    private int power;
    @XmlAttribute(name = "maxXp")
    private int maxXp;

    public float getRunSpeedFight() {
        return runSpeedFight;
    }

    /**
     * @return the pdef
     */
    public int getPdef() {
        return pdef;
    }

    /**
     * @return the mdef
     */
    public float getMdef() {
        return mdef;
    }

    /**
     * @return the mresist
     */
    public int getMresist() {
        return mresist;
    }

    /**
     * @return the crit
     */
    public float getCrit() {
        return crit;
    }

    /**
     * @return the accuracy
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * @return the power
     */
    public int getPower() {
        return power;
    }

    /**
     * @return the maxXp
     */
    public int getMaxXp() {
        return maxXp;
    }

}
