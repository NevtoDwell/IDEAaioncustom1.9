/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MpCondition")
public class MpCondition extends Condition {

    @XmlAttribute(required = true)
    protected int value;

    @XmlAttribute
    protected int delta;

    @XmlAttribute
    protected boolean ratio;

    @Override
    public boolean validate(Skill skill) {
        int valueWithDelta = value + delta * skill.getSkillLevel();
        if (ratio) {
            valueWithDelta = (skill.getEffector().getLifeStats().getMaxMp() * valueWithDelta) / 100;
        }
        return skill.getEffector().getLifeStats().getCurrentMp() > valueWithDelta;
    }
}
