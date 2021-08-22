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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.container.CreatureLifeStats;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SwitchHpMpEffect")
public class SwitchHpMpEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        CreatureLifeStats<? extends Creature> lifeStats = effect.getEffected().getLifeStats();
        int currentHp = lifeStats.getCurrentHp();
        int currentMp = lifeStats.getCurrentMp();

        lifeStats.increaseHp(TYPE.NATURAL_HP, currentMp - currentHp);
        lifeStats.increaseMp(TYPE.NATURAL_MP, currentHp - currentMp);
    }
}
