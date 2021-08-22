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
 * @author IlBuono
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "petstats")
public class PetStatsTemplate {

    @XmlAttribute(name = "reaction")
    private String reaction;
    @XmlAttribute(name = "run_speed")
    private float runSpeed;
    @XmlAttribute(name = "walk_speed")
    private float walkSpeed;
    @XmlAttribute(name = "height")
    private float height;
    @XmlAttribute(name = "altitude")
    private float altitude;

    public String getReaction() {
        return reaction;
    }

    public float getRunSpeed() {
        return runSpeed;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public float getHeight() {
        return height;
    }

    public float getAltitude() {
        return altitude;
    }
}
