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
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Sippolo
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpellAtkDrainEffect")
public class SpellAtkDrainEffect extends AbstractOverTimeEffect {

    @XmlAttribute(name = "hp_percent")
    protected int hp_percent;
    @XmlAttribute(name = "mp_percent")
    protected int mp_percent;

    @Override
    public void onPeriodicAction(Effect effect) {
        int valueWithDelta = value + delta * effect.getSkillLevel();
        int critAddDmg = critAddDmg2 + critAddDmg1 * effect.getSkillLevel();
        int damage = AttackUtil.calculateMagicalOverTimeSkillResult(effect, valueWithDelta, element, position, true, critProbMod2, critAddDmg);
        effect.getEffected().getController().onAttack(effect.getEffector(), effect.getSkillId(), TYPE.REGULAR, damage, true, LOG.SPELLATKDRAIN, effect.getAttackStatus());
        effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected());

        // Drain (heal) portion of damage inflicted
        if (hp_percent != 0) {
            effect.getEffector().getLifeStats().increaseHp(TYPE.HP, damage * hp_percent / 100, effect.getSkillId(), LOG.SPELLATKDRAIN);
        }
        if (mp_percent != 0) {
            effect.getEffector().getLifeStats().increaseMp(TYPE.MP, damage * mp_percent / 100, effect.getSkillId(), LOG.SPELLATKDRAIN);
        }
    }
}
