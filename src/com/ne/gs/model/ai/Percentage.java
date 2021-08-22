/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.ai;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Percentage")
public class Percentage {

    @XmlAttribute(name = "percent")
    protected int percent;
    @XmlAttribute(name = "skillId")
    protected int skillId = 0;
    @XmlAttribute(name = "isIndividual")
    protected boolean isIndividual = false;
    @XmlElement(name = "summonGroup")
    protected List<SummonGroup> summons;

    public List<SummonGroup> getSummons() {
        return summons;
    }

    public int getPercent() {
        return percent;
    }

    public int getSkillId() {
        return skillId;
    }

    public boolean isIndividual() {
        return isIndividual;
    }
}
