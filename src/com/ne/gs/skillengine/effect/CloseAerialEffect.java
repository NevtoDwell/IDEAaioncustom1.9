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
import com.ne.gs.skillengine.model.SpellStatus;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CloseAerialEffect")
public class CloseAerialEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        effect.getEffected().getEffectController().removeEffect(8224);
        effect.getEffected().getEffectController().removeEffect(8678);
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, null, SpellStatus.CLOSEAERIAL);
    }
}
