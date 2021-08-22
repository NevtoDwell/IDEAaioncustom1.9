/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MpUseAction")
public class MpUseAction extends Action {

    @XmlAttribute(required = true)
    protected int value;

    @XmlAttribute
    protected int delta;

    @XmlAttribute
    protected boolean ratio;

    @Override
    public void act(Skill skill) {
        Creature effector = skill.getEffector();
        int valueWithDelta = value + delta * skill.getSkillLevel();
        if (ratio) {
            valueWithDelta = (skill.getEffector().getLifeStats().getMaxMp() * valueWithDelta) / 100;
        }
        int changeMpPercent = skill.getBoostSkillCost();
        if (changeMpPercent != 0) {
            // changeMpPercent is negative
            valueWithDelta -= valueWithDelta / (100 / changeMpPercent);
        }

        effector.getLifeStats().reduceMp(valueWithDelta);
    }

}
