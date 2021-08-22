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
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NoFlyingCondition")
public class NoFlyingCondition extends Condition {

    @Override
    public boolean validate(Skill env) {
        return (!env.getEffector().isFlying());
    }

    @Override
    public boolean validate(Effect effect) {
        return (!effect.getEffected().isFlying());
    }

}
