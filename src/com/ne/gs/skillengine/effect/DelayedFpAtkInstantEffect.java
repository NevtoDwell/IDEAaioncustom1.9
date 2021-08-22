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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.ThreadPoolManager;

public class DelayedFpAtkInstantEffect extends EffectTemplate {

    @XmlAttribute
    protected int delay;

    @XmlAttribute
    protected boolean percent;

    public void calculate(Effect effect) {
        if ((effect.getEffected() instanceof Player)) {
            super.calculate(effect, null, null);
        }
    }

    public void applyEffect(final Effect effect) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            public void run() {
                if (effect.getEffector().isEnemy(effect.getEffected())) {
                    DelayedFpAtkInstantEffect.this.calculateAndApplyDamage(effect);
                }
            }
        }, delay);
    }

    private void calculateAndApplyDamage(Effect effect) {
        if (!(effect.getEffected() instanceof Player)) {
            return;
        }
        int valueWithDelta = value + delta * effect.getSkillLevel();
        Player player = (Player) effect.getEffected();
        int maxFP = player.getLifeStats().getMaxFp();

        int newValue = valueWithDelta;

        if (percent) {
            newValue = maxFP * valueWithDelta / 100;
        }
        player.getLifeStats().reduceFp(newValue);
    }
}
