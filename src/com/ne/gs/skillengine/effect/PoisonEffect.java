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

import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PoisonEffect")
public class PoisonEffect extends AbstractOverTimeEffect {

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.POISON_RESISTANCE, null);
    }

    @Override
    public void startEffect(Effect effect) {
        super.startEffect(effect, AbnormalState.POISON);
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect, AbnormalState.POISON);
    }

    @Override
    public void onPeriodicAction(Effect effect) {
        Creature effected = effect.getEffected();
        Creature effector = effect.getEffector();
        int valueWithDelta = value + delta * effect.getSkillLevel();
        int critAddDmg = critAddDmg2 + critAddDmg1 * effect.getSkillLevel();
        int damage = AttackUtil.calculateMagicalOverTimeSkillResult(effect, valueWithDelta, element, position, false, critProbMod2, critAddDmg);
        effected.getController().onAttack(effector, effect.getSkillId(), TYPE.DAMAGE, damage, false, LOG.POISON, null);
        effected.getObserveController().notifyDotAttackedObservers(effector, effect);
    }
}
