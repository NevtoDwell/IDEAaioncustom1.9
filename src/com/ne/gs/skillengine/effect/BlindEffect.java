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

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.controllers.observer.AttackStatusObserver;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BlindEffect")
public class BlindEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.BLIND_RESISTANCE, null);
    }

    @Override
    public void startEffect(Effect effect) {
        effect.setAbnormal(AbnormalState.BLIND.getId());
        effect.getEffected().getEffectController().setAbnormal(AbnormalState.BLIND.getId());
        AttackCalcObserver acObserver = new AttackStatusObserver(value, AttackStatus.DODGE) {
            @Override
            public boolean checkAttackerStatus(AttackStatus status) {
                return Rnd.chance(value);
            }
        };
        effect.getEffected().getObserveController().addAttackCalcObserver(acObserver);
        effect.setAttackStatusObserver(acObserver, position);
    }

    @Override
    public void endEffect(Effect effect) {
        AttackCalcObserver acObserver = effect.getAttackStatusObserver(position);
        if (acObserver != null) {
            effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
        }
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.BLIND.getId());
    }

}
