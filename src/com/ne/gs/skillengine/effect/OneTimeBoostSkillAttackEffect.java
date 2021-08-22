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

import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OneTimeBoostSkillAttackEffect")
public class OneTimeBoostSkillAttackEffect extends BufEffect {

    @XmlAttribute
    private int count;

    @XmlAttribute
    private SkillType type;

    @Override
    public void startEffect(final Effect effect) {
        super.startEffect(effect);

        final int stopCount = count;
        final float percent = 1.0f + value / 100.0f;
        AttackCalcObserver observer = null;

        switch (type) {
            case MAGICAL:
                observer = new AttackCalcObserver() {

                    private int count = 0;

                    @Override
                    public float getBaseMagicalDamageMultiplier() {
                        if (count++ < stopCount) {
                            return percent;
                        } else {
                            effect.getEffected().getEffectController().removeEffect(effect.getSkillId());
                        }

                        return 1.0f;
                    }
                };
                break;
            case PHYSICAL:
                observer = new AttackCalcObserver() {

                    private int count = 0;

                    @Override
                    public float getBasePhysicalDamageMultiplier(boolean isSkill) {
                        if (!isSkill) {
                            return 1f;
                        }

                        if (count++ < stopCount) {
                            if (count == stopCount) {
                                effect.getEffected().getEffectController().removeEffect(effect.getSkillId());
                            }
                            return percent;
                        }

                        return 1.0f;
                    }
                };
                break;
        }

        effect.getEffected().getObserveController().addAttackCalcObserver(observer);
        effect.setAttackStatusObserver(observer, position);
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect);
        AttackCalcObserver observer = effect.getAttackStatusObserver(position);
        effect.getEffected().getObserveController().removeAttackCalcObserver(observer);
    }
}
