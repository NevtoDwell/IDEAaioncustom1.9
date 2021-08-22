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
@XmlType(name = "CurseEffect")
public class CurseEffect extends BufEffect {

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.CURSE_RESISTANCE, null);
    }

    @Override
    public void startEffect(Effect effect) {
        super.startEffect(effect);
        effect.setAbnormal(AbnormalState.CURSE.getId());
        effect.getEffected().getEffectController().setAbnormal(AbnormalState.CURSE.getId());
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect);
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.CURSE.getId());
    }
}
