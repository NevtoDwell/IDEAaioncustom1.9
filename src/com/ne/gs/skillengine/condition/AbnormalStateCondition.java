/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.condition;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author kecimis
 */
public class AbnormalStateCondition extends Condition {

    @XmlAttribute(required = true)
    protected AbnormalState value;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.skillengine.condition.Condition#validate(com.ne.gs.skillengine.model.Skill)
     */
    @Override
    public boolean validate(Skill env) {
        if (env.getFirstTarget() != null) {
            return (env.getFirstTarget().getEffectController().isAbnormalSet(value));
        }
        return false;
    }

    @Override
    public boolean validate(Effect effect) {
        if (effect.getEffected() != null) {
            return (effect.getEffected().getEffectController().isAbnormalSet(value));
        }
        return false;
    }

}
