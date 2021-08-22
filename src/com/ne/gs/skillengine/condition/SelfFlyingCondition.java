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

import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.FlyingRestriction;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SelfFlyingCondition")
public class SelfFlyingCondition extends Condition {

    @XmlAttribute(required = true)
    protected FlyingRestriction restriction;

    @Override
    public boolean validate(Skill env) {
        if (env.getEffector() == null) {
            return false;
        }

        switch (restriction) {
            case FLY:
                return env.getEffector().isFlying();
            case GROUND:
                return !env.getEffector().isFlying();
        }

        return true;
    }

    @Override
    public boolean validate(Effect effect) {
        if (effect.getEffector() == null) {
            return false;
        }

        switch (restriction) {
            case FLY:
                return effect.getEffector().isFlying();
            case GROUND:
                return !effect.getEffector().isFlying();
        }

        return true;
    }

}
