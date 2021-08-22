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

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiseaseEffect")
public class DiseaseEffect extends EffectTemplate {

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.DISEASE_RESISTANCE, null);
    }

    // skillId 18386
    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void startEffect(Effect effect) {
        Creature effected = effect.getEffected();
        effect.setAbnormal(AbnormalState.DISEASE.getId());
        effected.getEffectController().setAbnormal(AbnormalState.DISEASE.getId());
    }

    @Override
    public void endEffect(Effect effect) {
        if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.DISEASE)) {
            effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.DISEASE.getId());
        }
    }

}
