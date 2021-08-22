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

import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConfuseEffect")
public class ConfuseEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        // TODO Auto-generated method stub
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.CONFUSE_RESISTANCE, null);
    }

    @Override
    public void startEffect(Effect effect) {
        super.startEffect(effect);
    }

}
