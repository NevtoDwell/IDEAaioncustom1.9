/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedalReward")
public class MedalItem extends IdLevelReward {

    @XmlAttribute(name = "count")
    protected int count;

    @XmlAttribute(name = "chance")
    protected float chance;

    public int getCount() {
        return count;
    }

    public float getChance() {
        return chance;
    }
}
