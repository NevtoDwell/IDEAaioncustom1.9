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

import com.ne.gs.model.gameobjects.SummonedObject;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author Tomate
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HpCondition")
public class HpCondition extends Condition {

    @XmlAttribute(required = true)
    protected int value;

    @XmlAttribute
    protected int delta;

    @XmlAttribute
    protected boolean ratio;

    @Override
    public boolean validate(Skill skill) {
        // exception for Servants, Totems to let them cast last skill and die
        if (skill.getEffector() instanceof SummonedObject) {
            return true;
        }

        int valueWithDelta = value + delta * skill.getSkillLevel();
        if (ratio) {
            valueWithDelta = (int) (valueWithDelta / 100f * skill.getEffector().getLifeStats().getMaxHp());
        }
        return skill.getEffector().getLifeStats().getCurrentHp() > valueWithDelta;
    }
}
