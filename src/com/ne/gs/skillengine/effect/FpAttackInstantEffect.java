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

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FpAttackInstantEffect")
public class FpAttackInstantEffect extends EffectTemplate {

    @XmlAttribute
    protected boolean percent;

    @Override
    public void calculate(Effect effect) {
        // Only players have FP
        if (effect.getEffected() instanceof Player) {
            super.calculate(effect, null, null);
        }
    }

    @Override
    public void applyEffect(Effect effect) {
        // Restriction to players because lack of FP on other Creatures
        if (!(effect.getEffected() instanceof Player)) {
            return;
        }
        Player player = (Player) effect.getEffected();
        int maxFP = player.getLifeStats().getMaxFp();
        int newValue = value;
        // Support for values in percentage
        if (percent) {
            newValue = (maxFP * value) / 100;
        }
        player.getLifeStats().reduceFp(newValue);
    }
}
