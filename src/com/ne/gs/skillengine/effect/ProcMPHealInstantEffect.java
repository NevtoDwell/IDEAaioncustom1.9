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

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcMPHealInstantEffect")
public class ProcMPHealInstantEffect extends AbstractHealEffect {

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, HealType.MP);
    }

    @Override
    public void applyEffect(Effect effect) {
        super.applyEffect(effect, HealType.MP);
    }

    @Override
    public int getCurrentStatValue(Effect effect) {
        return effect.getEffected().getLifeStats().getCurrentMp();
    }

    @Override
    public int getMaxStatValue(Effect effect) {
        return effect.getEffected().getGameStats().getMaxMp().getCurrent();
    }
}
