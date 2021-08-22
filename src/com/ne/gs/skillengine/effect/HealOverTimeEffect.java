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
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;

/**
 * @author ATracer
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HealOverTimeEffect")
public abstract class HealOverTimeEffect
        extends AbstractOverTimeEffect
        implements HealEffectCalc.HealEffectApi {

    public void calculate(Effect effect, HealType healType) {
        if (!super.calculate(effect, null, null)) {
            return;
        }

        Creature effector = effect.getEffector();
        if (effect.getEffected() instanceof Npc) {
            value = effector.getAi2().modifyHealValue(value);
        }
        int valueWithDelta = value + delta * effect.getSkillLevel();
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
                if (boostHealAdd > 0) {
                    boostHeal += boostHeal * boostHealAdd / 1000;
                }
                finalHeal = effector.getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, boostHeal).getCurrent();
            }
            finalHeal = effector.getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, finalHeal).getCurrent();
        }
        effect.setReservedInt(position, finalHeal);
        effect.addSucessEffect(this);
    }

    public void onPeriodicAction(Effect effect, HealType healType) {
        Creature effected = effect.getEffected();

        int possibleHealValue = effect.getReservedInt(position);
        int healValue = HealEffectCalc.limit(this, effect, possibleHealValue);

        if (healValue <= 0) {
            return;
        }

        switch (healType) {
            case HP:
                effected.getLifeStats().increaseHp(TYPE.HP, healValue, effect.getSkillId(), LOG.HEAL);
                break;
            case MP:
                effected.getLifeStats().increaseMp(TYPE.MP, healValue, effect.getSkillId(), LOG.MPHEAL);
                break;
            case FP:
                ((Player) effected).getLifeStats().increaseFp(TYPE.FP, healValue, effect.getSkillId(), LOG.FPHEAL);
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
