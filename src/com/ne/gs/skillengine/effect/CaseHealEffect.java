/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;

public class CaseHealEffect extends AbstractHealEffect {

    @XmlAttribute(name = "cond_value")
    protected int condValue;

    @XmlAttribute
    protected HealType type;

    public int getCurrentStatValue(Effect effect) {
        if (type == HealType.HP) {
            return effect.getEffected().getLifeStats().getCurrentHp();
        }
        if (type == HealType.MP) {
            return effect.getEffected().getLifeStats().getCurrentMp();
        }
        return 0;
    }

    public int getMaxStatValue(Effect effect) {
        if (type == HealType.HP) {
            return effect.getEffected().getGameStats().getMaxHp().getCurrent();
        }
        if (type == HealType.MP) {
            return effect.getEffected().getGameStats().getMaxMp().getCurrent();
        }
        return 0;
    }

    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    public void endEffect(Effect effect) {
        ActionObserver observer = effect.getActionObserver(position);
    	if(observer != null) {
    		effect.getEffected().getObserveController().removeObserver(observer);
            effect.setActionObserver(null, position);
    	}
    }

    public void startEffect(final Effect effect) {
        if (healPlayer(effect)) {
            return;
        }
        ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {
            public void attacked(Creature creature) {
                healPlayer(effect);
            }
        };
        effect.getEffected().getObserveController().addObserver(observer);
        effect.setActionObserver(observer, position);
    }

    private boolean healPlayer(Effect effect) {
        final int valueWithDelta = value + delta * effect.getSkillLevel();
        final int currentValue = getCurrentStatValue(effect);
        final int maxValue = getMaxStatValue(effect);
        if (currentValue <= (maxValue * condValue / 100)) {
            int possibleHealValue = 0;
            if (percent) {
                possibleHealValue = maxValue * valueWithDelta / 100;
            } else {
                possibleHealValue = valueWithDelta;
            }

            int finalHeal = effect.getEffected().getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, possibleHealValue)
                    .getCurrent();
            finalHeal = effect.getEffected().getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, finalHeal).getCurrent();
            finalHeal = maxValue - currentValue < finalHeal ? (maxValue - currentValue) : finalHeal;

            if (type == HealType.HP && effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.DISEASE)) {
                finalHeal = 0;
            }

            // apply heal
            if (type == HealType.HP) {
                effect.getEffected().getLifeStats().increaseHp(TYPE.HP, finalHeal, effect.getSkillId(), LOG.REGULAR);
            } else if (type == HealType.MP) {
                effect.getEffected().getLifeStats().increaseMp(TYPE.MP, finalHeal, effect.getSkillId(), LOG.REGULAR);
            }
            effect.endEffect();
            return true;
        }
        return false;
    }
}
