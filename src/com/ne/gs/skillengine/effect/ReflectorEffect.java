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

import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.controllers.observer.AttackShieldObserver;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ginho1 modified by Wakizashi, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReflectorEffect")
public class ReflectorEffect extends ShieldEffect {

    @Override
    public void startEffect(Effect effect) {
        int hit = hitvalue + hitdelta * effect.getSkillLevel();

        AttackShieldObserver asObserver = new AttackShieldObserver(hit, value, percent, false, effect, hitType, getType(), hitTypeProb, minradius, radius, null);

        effect.getEffected().getObserveController().addAttackCalcObserver(asObserver);
        effect.setAttackShieldObserver(asObserver, position);
    }

    @Override
    public void endEffect(Effect effect) {
        AttackCalcObserver acObserver = effect.getAttackShieldObserver(position);
        if (acObserver != null) {
            effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
        }
    }

    /**
     * shieldType 1:reflector 2: normal shield 8: protec
     *
     * @return
     */
    @Override
    public int getType() {
        return 1;
    }
}
