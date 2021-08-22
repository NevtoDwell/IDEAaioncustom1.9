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
 * @author Sippolo
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetFlyingCondition")
public class TargetFlyingCondition extends Condition {

    @XmlAttribute(required = true)
    protected FlyingRestriction restriction = FlyingRestriction.FLY;

    @Override
    public boolean validate(Skill env) {
        if (env.getFirstTarget() == null) {
            return false;
        }

        switch (restriction) {
            case FLY:
                return env.getFirstTarget().isFlying();
            case GROUND:
                return !env.getFirstTarget().isFlying();
        }

        return true;
    }

    @Override
    public boolean validate(Effect effect) {
        if (effect.getEffected() == null) {
            return false;
        }

        switch (restriction) {
            case FLY:
                return effect.getEffected().isFlying();
            case GROUND:
                return !effect.getEffected().isFlying();
        }

        return true;
    }

}
