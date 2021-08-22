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
@XmlType(name = "HpUseAction")
public class HpUseAction extends Action {

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
            valueWithDelta = (int) (valueWithDelta / 100f * skill.getEffector().getLifeStats().getMaxHp());
        }

        effector.getLifeStats().reduceHp(valueWithDelta, effector);
    }

}
