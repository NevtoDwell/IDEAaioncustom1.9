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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.stats.calc.functions.StatAddFunction;
import com.ne.gs.model.stats.calc.functions.StatRateFunction;
import com.ne.gs.model.stats.calc.functions.StatSetFunction;
import com.ne.gs.model.stats.container.CreatureGameStats;
import com.ne.gs.model.stats.container.CreatureLifeStats;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.change.Change;
import com.ne.gs.skillengine.condition.Conditions;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BufEffect")
public abstract class BufEffect extends EffectTemplate {

    @XmlAttribute
    protected boolean maxstat;

    private static final Logger log = LoggerFactory.getLogger(BufEffect.class);

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    /**
     * Will be called from effect controller when effect ends
     */
    @Override
    public void endEffect(Effect effect) {
        Creature effected = effect.getEffected();
        effected.getGameStats().endEffect(effect);
    }

    /**
     * Will be called from effect controller when effect starts
     */
    @Override
    public void startEffect(Effect effect) {
        if (change == null) {
            return;
        }

        Creature effected = effect.getEffected();
        CreatureGameStats<? extends Creature> cgs = effected.getGameStats();
        CreatureLifeStats<? extends Creature> cls = effected.getLifeStats();

        List<IStatFunction> modifiers = getModifiers(effect);

        if (modifiers.size() > 0) {
            cgs.addEffect(effect, modifiers);
        }

        if (maxstat) {
            cls.increaseHp(TYPE.HP, cgs.getMaxHp().getCurrent());
            cls.increaseMp(TYPE.HEAL_MP, cgs.getMaxMp().getCurrent());
        }
    }

    /**
     * @param effect
     *
     * @return
     */
    protected List<IStatFunction> getModifiers(Effect effect) {
        int skillId = effect.getSkillId();
        int skillLvl = effect.getSkillLevel();

        List<IStatFunction> modifiers = new ArrayList<>();

        for (Change changeItem : change) {
            if (changeItem.getStat() == null) {
                log.warn("Skill stat has wrong name for skillid: " + skillId);
                continue;
            }

            int valueWithDelta = changeItem.getValue() + changeItem.getDelta() * skillLvl;

            Conditions conditions = changeItem.getConditions();
            switch (changeItem.getFunc()) {
                case ADD:
                    modifiers.add(new StatAddFunction(changeItem.getStat(), valueWithDelta, true).withConditions(conditions));
                    break;
                case PERCENT:
                    modifiers.add(new StatRateFunction(changeItem.getStat(), valueWithDelta, true).withConditions(conditions));
                    break;
                case REPLACE:
                    modifiers.add(new StatSetFunction(changeItem.getStat(), valueWithDelta, true).withConditions(conditions));
                    break;
            }
        }
        return modifiers;
    }

    @Override
    public void onPeriodicAction(Effect effect) {
        // TODO Auto-generated method stub
    }
}
