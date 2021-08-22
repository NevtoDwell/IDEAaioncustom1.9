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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelayedFPAttackInstantEffect")
public class DelayedFPAttackInstantEffect extends EffectTemplate {

    @XmlAttribute
    protected int delay;
    @XmlAttribute
    protected boolean percent;

    @Override
    public void calculate(Effect effect) {
        if (!(effect.getEffected() instanceof Player)) {
            return;
        }
        if (!super.calculate(effect, null, null)) {
            return;
        }

        int maxFP = ((Player) effect.getEffected()).getLifeStats().getMaxFp();
        int newValue = (percent) ? (maxFP * value) / 100 : value;

        effect.setReserved2(newValue);
    }

    @Override
    public void applyEffect(Effect effect) {
        final Player effected = (Player) effect.getEffected();
        final int newValue = effect.getReserved2();

        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                effected.getLifeStats().reduceFp(newValue);
            }
        }, delay);
    }
}
