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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.controllers.observer.AttackerCriticalStatus;
import com.ne.gs.controllers.observer.AttackerCriticalStatusObserver;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OneTimeBoostSkillCriticalEffect")
public class OneTimeBoostSkillCriticalEffect extends EffectTemplate {

    @XmlAttribute
    private int count;
    @XmlAttribute
    private boolean percent;

    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void startEffect(final Effect effect) {
        super.startEffect(effect);

        AttackerCriticalStatusObserver observer = new AttackerCriticalStatusObserver(AttackStatus.CRITICAL, count, value, percent) {

            public AttackerCriticalStatus checkAttackerCriticalStatus(AttackStatus stat, boolean isSkill) {
                if ((stat == status) && (isSkill)) {
                    if (getCount() <= 1) {
                        effect.endEffect();
                    } else {
                        decreaseCount();
                    }
                    acStatus.setResult(true);
                } else {
                    acStatus.setResult(false);
                }
                return acStatus;
            }
        };
        effect.getEffected().getObserveController().addAttackCalcObserver(observer);
        effect.setAttackStatusObserver(observer, position);
    }

    public void endEffect(Effect effect) {
        super.endEffect(effect);

        AttackCalcObserver observer = effect.getAttackStatusObserver(position);
        effect.getEffected().getObserveController().removeAttackCalcObserver(observer);
    }

    public boolean isPercent() {
        return percent;
    }
}
