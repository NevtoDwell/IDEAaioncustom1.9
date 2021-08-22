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

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.controllers.observer.AttackShieldObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Sippolo modified by kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectEffect")
public class ProtectEffect extends ShieldEffect {

    @Override
    public void startEffect(final Effect effect) {

        AttackShieldObserver asObserver = new AttackShieldObserver(value, this.hitvalue,
                radius, percent, effect, this.hitType, this.getType(), this.hitTypeProb);

        effect.getEffected().getObserveController().addAttackCalcObserver(asObserver);
        effect.setAttackShieldObserver(asObserver, position);
        if ((effect.getEffector() instanceof Summon)) {
            ActionObserver summonRelease = new ActionObserver(ObserverType.SUMMONRELEASE) {

                public void summonrelease() {
                    effect.endEffect();
                }
            };
            effect.getEffector().getObserveController().attach(summonRelease);
            effect.setActionObserver(summonRelease, position);
        } else {
            ActionObserver death = new ActionObserver(ObserverType.DEATH) {

                public void died(Creature creature) {
                    effect.endEffect();
                }
            };
            effect.getEffector().getObserveController().attach(death);
            effect.setActionObserver(death, position);
        }
    }

    @Override
    public void endEffect(Effect effect) {
        AttackCalcObserver acObserver = effect.getAttackShieldObserver(position);
        if (acObserver != null) {
            effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
        }
        ActionObserver aObserver = effect.getActionObserver(position);
        if (aObserver != null) {
            effect.getEffector().getObserveController().removeObserver(aObserver);
        }
    }

    /**
     * shieldType 1:reflector 2: normal shield 8: protec
     *
     * @return
     */
    @Override
    public int getType() {
        return 8;
    }
}
