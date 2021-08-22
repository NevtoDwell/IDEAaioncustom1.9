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
import com.ne.gs.skillengine.model.HealType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DPHealInstantEffect")
public class DPHealInstantEffect extends AbstractHealEffect {

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, HealType.DP);
    }

    @Override
    public void applyEffect(Effect effect) {
        super.applyEffect(effect, HealType.DP);
    }

    @Override
    public int getCurrentStatValue(Effect effect) {
        // TODO what about traps??
        // java.lang.ClassCastException: com.ne.gs.model.gameobjects.Trap cannot be cast to com.ne.gs.model.gameobjects.player.Player
	    //   at com.ne.gs.skillengine.effect.DPHealInstantEffect.getCurrentStatValue(DPHealInstantEffect.java:38) ~[gameserver-3.0.1.jar:3.0.1]
        return effect.getEffected().getLifeStats().getCurrentDp();
    }

    @Override
    public int getMaxStatValue(Effect effect) {
        return ((Player) effect.getEffected()).getGameStats().getMaxDp().getCurrent();
    }
}
