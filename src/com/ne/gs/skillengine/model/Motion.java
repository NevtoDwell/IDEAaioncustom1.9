/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * @author kecimis
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Motion")
public class Motion {

    @XmlAttribute(required = true)
    protected String name;// TODO enum

    @XmlAttribute
    protected int speed = 100;

    @XmlAttribute(name = "instant_skill")
    protected boolean instantSkill = false;

    public String getName() {
        return this.name;
    }

    public int getSpeed() {
        return this.speed;
    }

    public boolean getInstantSkill() {
        return this.instantSkill;
    }
}
