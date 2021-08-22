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

import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.effect.modifier.ActionModifier;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelayedSpellAttackInstantEffect")
public class DelayedSpellAttackInstantEffect extends DamageEffect {

    @XmlAttribute
    protected int delay;

    @Override
    public void applyEffect(final Effect effect) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            public void run() {
                if (effect.getEffector().isEnemy(effect.getEffected())) {
                    DelayedSpellAttackInstantEffect.this.calculateAndApplyDamage(effect);
                }
            }
        }, delay);
    }

    private void calculateAndApplyDamage(Effect effect) {
        int skillLvl = effect.getSkillLevel();
        int valueWithDelta = value + delta * skillLvl;
        ActionModifier modifier = getActionModifiers(effect);
        int critAddDmg = critAddDmg2 + critAddDmg1 * effect.getSkillLevel();
        AttackUtil.calculateMagicalSkillResult(effect, valueWithDelta, modifier, getElement(), true, true, false, getMode(), critProbMod2, critAddDmg);
        effect.getEffected().getController().onAttack(effect.getEffector(), effect.getSkillId(), TYPE.DELAYDAMAGE, effect.getReserved1(), true, LOG.PROCATKINSTANT, effect.getAttackStatus());
        effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected());
    }
}
