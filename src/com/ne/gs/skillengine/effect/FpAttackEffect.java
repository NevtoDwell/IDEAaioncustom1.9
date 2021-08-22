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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FpAttackEffect")
public class FpAttackEffect extends AbstractOverTimeEffect {

    @Override
    public void calculate(Effect effect) {
        // Only players have FP
        if (effect.getEffected() instanceof Player) {
            super.calculate(effect, null, null);
        }
    }

    @Override
    public void onPeriodicAction(Effect effect) {
        Player effected = (Player) effect.getEffected();
        int maxFP = effected.getLifeStats().getMaxFp();
        int newValue = value;
        // Support for values in percentage
        if (percent) {
            newValue = (maxFP * value) / 100;
        }
        effected.getLifeStats().reduceFp(newValue);
    }
}
