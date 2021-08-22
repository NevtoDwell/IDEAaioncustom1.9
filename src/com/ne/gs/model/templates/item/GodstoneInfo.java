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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Godstone")
public class GodstoneInfo {

    @XmlAttribute
    private int skillid;
    @XmlAttribute
    private int skilllvl;
    @XmlAttribute
    private int probability;
    @XmlAttribute
    private int probabilityleft;

    /**
     * @return the skillid
     */
    public int getSkillid() {
        return skillid;
    }

    /**
     * @return the skilllvl
     */
    public int getSkilllvl() {
        return skilllvl;
    }

    /**
     * @return the probability
     */
    public int getProbability() {
        return probability;
    }

    /**
     * @return the probabilityleft
     */
    public int getProbabilityleft() {
        return probabilityleft;
    }
}
