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
@XmlType(name = "DPTransferEffect")
public class DPTransferEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        ((Player) effect.getEffected()).getLifeStats().reduceDp(effect.getReserved1());
        ((Player) effect.getEffector()).getLifeStats().increaseDp(effect.getReserved1());
    }

    @Override
    public void calculate(Effect effect) {
        if (!super.calculate(effect, null, null)) {
            return;
        }
        effect.setReserved1(-getCurrentStatValue(effect));
    }

    private int getCurrentStatValue(Effect effect) {
        return ((Player) effect.getEffector()).getLifeStats().getCurrentDp();
    }

    @SuppressWarnings("unused")
    private int getEffectedCurrentStatValue(Effect effect) {
        return ((Player) effect.getEffected()).getLifeStats().getCurrentDp();
    }

    @SuppressWarnings("unused")
    private int getMaxStatValue(Effect effect) {
        return ((Player) effect.getEffected()).getGameStats().getMaxDp().getCurrent();
    }
}
