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
import com.ne.gs.utils.PositionUtil;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BackCondition")
public class BackCondition extends Condition {

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.skillengine.condition.Condition#validate(com.ne.gs.skillengine.model.Skill)
     */
    @Override
    public boolean validate(Skill env) {
        if (env.getFirstTarget() == null || env.getEffector() == null) {
            return false;
        }

        return PositionUtil.isBehindTarget(env.getEffector(), env.getFirstTarget());
    }

    @Override
    public boolean validate(Effect effect) {
        if (effect.getEffected() == null || effect.getEffector() == null) {
            return false;
        }

        return PositionUtil.isBehindTarget(effect.getEffector(), effect.getEffected());
    }

}
