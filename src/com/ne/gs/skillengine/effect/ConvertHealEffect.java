/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.controllers.observer.AttackShieldObserver;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;

public class ConvertHealEffect extends ShieldEffect {

    @XmlAttribute
    protected HealType type;

    @XmlAttribute(name = "hitpercent")
    protected boolean hitPercent;

    public void startEffect(Effect effect) {
        int skillLvl = effect.getSkillLevel();
        int valueWithDelta = value + delta * skillLvl;
        int hitValueWithDelta = hitvalue + hitdelta * skillLvl;

        AttackShieldObserver asObserver = new AttackShieldObserver(hitValueWithDelta, valueWithDelta, percent, hitPercent, effect, hitType, getType(), hitTypeProb, 0, 0, type);

        effect.getEffected().getObserveController().addAttackCalcObserver(asObserver);
        effect.setAttackShieldObserver(asObserver, position);
        effect.getEffected().getEffectController().setUnderShield(true);
    }

    public int getType() {
        return 0;
    }
}
