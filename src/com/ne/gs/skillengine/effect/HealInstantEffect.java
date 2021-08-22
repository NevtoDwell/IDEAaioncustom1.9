/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;
import com.ne.gs.skillengine.model.TransformType;

/**
 * @author ATracer
 * @modified vlog, Sippolo, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HealInstantEffect")
public class HealInstantEffect extends AbstractHealEffect {

    @Override
    public void applyEffect(Effect effect) {
        super.applyEffect(effect, HealType.HP);
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, HealType.HP);
    }

    @Override
    public int getCurrentStatValue(Effect effect) {
        return effect.getEffected().getLifeStats().getCurrentHp();
    }

    @Override
    public int getMaxStatValue(Effect effect) {
        if(effect.getEffector().getEffectController().getTransformType()==TransformType.AVATAR){
            return effect.getEffected().getLifeStats().getMaxHp();
        }
        return effect.getEffected().getGameStats().getMaxHp().getCurrent();
    }
}