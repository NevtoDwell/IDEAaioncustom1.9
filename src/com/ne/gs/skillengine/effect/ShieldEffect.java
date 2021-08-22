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
import com.ne.gs.controllers.observer.AttackShieldObserver;
import com.ne.gs.model.Race;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer modified by Wakizashi, Sippolo, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShieldEffect")
public class ShieldEffect extends EffectTemplate {

    @XmlAttribute
    protected int hitdelta;
    @XmlAttribute
    protected int hitvalue;
    @XmlAttribute
    protected boolean percent;
    @XmlAttribute
    protected int radius = 0;
    @XmlAttribute
    protected int minradius = 0;
    @XmlAttribute
    protected Race condrace = null;

    @Override
    public void applyEffect(Effect effect) {
        // check for condition race, skillId: 10317,10318
        if (condrace != null && effect.getEffected().getRace() != condrace) {
            return;
        }

        effect.addToEffectedController();
    }

    @Override
    public void calculate(Effect effect) {
        effect.addSucessEffect(this);
    }

    @Override
    public void startEffect(Effect effect) {
        int skillLvl = effect.getSkillLevel();
        int valueWithDelta = value + delta * skillLvl;
        int hitValueWithDelta = hitvalue + hitdelta * skillLvl;

        AttackShieldObserver asObserver = new AttackShieldObserver(hitValueWithDelta, valueWithDelta, percent, effect, hitType, getType(), hitTypeProb);

        effect.getEffected().getObserveController().addAttackCalcObserver(asObserver);
        effect.setAttackShieldObserver(asObserver, position);
        effect.getEffected().getEffectController().setUnderShield(true);
    }

    @Override
    public void endEffect(Effect effect) {
        AttackCalcObserver acObserver = effect.getAttackShieldObserver(position);
        if (acObserver != null) {
            effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
        }
        effect.getEffected().getEffectController().setUnderShield(false);
    }

    /**
     * shieldType 1:reflector 2: normal shield 8: protect
     *
     * @return
     */
    public int getType() {
        return 2;
    }

}
