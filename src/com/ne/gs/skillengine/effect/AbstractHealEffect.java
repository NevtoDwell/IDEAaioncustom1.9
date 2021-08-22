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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;

/**
 * @author ATracer modified by Wakizashi, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractHealEffect")
public abstract class AbstractHealEffect
    extends EffectTemplate
    implements HealEffectCalc.HealEffectApi {

    @XmlAttribute
    protected boolean percent;

    public void calculate(Effect effect, HealType healType) {
        if (!super.calculate(effect, null, null)) {
            return;
        }
        Creature effector = effect.getEffector();

        int valueWithDelta = value + delta * effect.getSkillLevel();
        int currentValue = getCurrentStatValue(effect);
        int maxCurValue = getMaxStatValue(effect);
        int possibleHealValue = 0;
        if (percent) {
            possibleHealValue = maxCurValue * valueWithDelta / 100;
        } else {
            possibleHealValue = valueWithDelta;
        }

        int finalHeal = possibleHealValue;

        if (healType == HealType.HP) {
            int baseHeal = possibleHealValue;
            if (effect.getItemTemplate() == null) {
                int boostHealAdd = effector.getGameStats().getStat(StatEnum.HEAL_BOOST, 0).getCurrent();
                // Apply percent Heal Boost bonus (ex. Passive skills)
                int boostHeal = (effector.getGameStats().getStat(StatEnum.HEAL_BOOST, baseHeal).getCurrent() - boostHealAdd);
                // Apply Add Heal Boost bonus (ex. Skills like Benevolence)
                boostHeal += boostHeal * boostHealAdd / 1000;
                finalHeal = effector.getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, boostHeal).getCurrent();
            }
            finalHeal = effector.getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, finalHeal).getCurrent();
        }

        if (finalHeal < 0) {
            finalHeal = currentValue > -finalHeal ? finalHeal : -currentValue;
        } else {
            finalHeal = maxCurValue - currentValue < finalHeal ? (maxCurValue - currentValue) : finalHeal;
        }

        if (healType == HealType.HP && effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.DISEASE)) {
            finalHeal = 0;
        }

        effect.setReservedInt(position, finalHeal);
        effect.setReserved1(-finalHeal);
    }

    public void applyEffect(Effect effect, HealType healType) {
        Creature effected = effect.getEffected();
        int healValue = effect.getReservedInt(position);

        if (healValue == 0) {
            return;
        }

        switch (healType) {
            case HP:
                // item heal, eg potions
                if (this instanceof ProcHealInstantEffect) {
                    effected.getLifeStats().increaseHp(TYPE.HP, healValue, 0, LOG.REGULAR);
                } else if (healValue > 0) {
                    effected.getLifeStats().increaseHp(TYPE.REGULAR, healValue, 0, LOG.REGULAR);
                } else {
                    // TODO shouldnt send value, on retail sm_attack_status is send only to update hp bar
                    effected.getLifeStats().reduceHp(-healValue, effected);
                }
                break;
            case MP:
                // item heal, eg potions
                if (this instanceof ProcMPHealInstantEffect) {
                    effected.getLifeStats().increaseMp(TYPE.MP, healValue, 0, LOG.REGULAR);
                } else {
                    effected.getLifeStats().increaseMp(TYPE.HEAL_MP, healValue, 0, LOG.REGULAR);
                }
                break;
            case FP:
                effected.getLifeStats().increaseFp(TYPE.FP, healValue);
                break;
            case DP:
                ((Player) effected).getLifeStats().increaseDp(healValue);
                break;
        }
    }

    @Override
    public boolean isPercent() {
        return percent;
    }

    public abstract int getCurrentStatValue(Effect effect);

    public abstract int getMaxStatValue(Effect effect);
}
