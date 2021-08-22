/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect.modifier;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author kecimis
 */
public class AbnormalDamageModifier extends ActionModifier {

    @XmlAttribute(required = true)
    protected AbnormalState state;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.skillengine.effect.modifier.ActionModifier#analyze(com.ne.gs.skillengine.model
     * .Effect)
     */
    @Override
    public int analyze(Effect effect) {
        return (value + effect.getSkillLevel() * delta);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.skillengine.effect.modifier.ActionModifier#check(com.ne.gs.skillengine.model
     * .Effect)
     */
    @Override
    public boolean check(Effect effect) {
        return effect.getEffected().getEffectController().isAbnormalSet(state);
    }

}
