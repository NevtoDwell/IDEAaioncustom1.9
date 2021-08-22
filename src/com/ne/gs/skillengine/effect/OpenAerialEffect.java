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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SpellStatus;

/**
 * @author ATracer //воздушные оковы
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenAerialEffect")
public class OpenAerialEffect extends EffectTemplate {
    
    @Override
    public void applyEffect(Effect effect) {

        if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STUMBLE))
            return;

        effect.addToEffectedController();
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.OPENAREIAL_RESISTANCE, SpellStatus.OPENAERIAL);
    }

    @Override
    public void startEffect(Effect effect) {
        Creature effected = effect.getEffected();
        effected.getController().cancelCurrentSkill();
        effected.getMoveController().abortMove();
        effected.getEffectController().setAbnormal(AbnormalState.OPENAERIAL.getId());
        effect.setAbnormal(AbnormalState.OPENAERIAL.getId());
        effect.getEffected().getEffectController().removeParalyzeEffects();
    }

    @Override
    public void endEffect(Effect effect) {
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.OPENAERIAL.getId());
    }
}
