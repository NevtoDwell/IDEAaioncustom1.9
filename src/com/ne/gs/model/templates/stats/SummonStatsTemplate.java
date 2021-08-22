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
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "summon_stats_template")
public class SummonStatsTemplate extends StatsTemplate {

    @XmlAttribute(name = "pdefense")
    private int pdefense;
    @XmlAttribute(name = "mresist")
    private int mresist;
    @XmlAttribute(name = "mcrit")
    private int mcrit;

    /**
     * @return the pdefense
     */
    public int getPdefense() {
        return pdefense;
    }

    /**
     * @return the mresist
     */
    public int getMresist() {
        return mresist;
    }

    /**
     * @return the mcrit
     */
    public int getMcrit() {
        return mcrit;
    }

}
