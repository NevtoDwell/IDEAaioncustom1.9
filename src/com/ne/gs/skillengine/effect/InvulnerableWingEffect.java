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
 * @author VladimirZ, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvulnerableWingEffect")
public class InvulnerableWingEffect extends EffectTemplate {

    @Override
    public void calculate(Effect effect) {
        // Only for players
        if (effect.getEffected() instanceof Player) {
            super.calculate(effect, null, null);
        }
    }

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
        ((Player) effect.getEffected()).setInvulnerableWing(true);
    }

    @Override
    public void endEffect(Effect effect) {
        ((Player) effect.getEffected()).setInvulnerableWing(false);
    }
}
