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
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NoReduceSpellATKInstantEffect")
public class NoReduceSpellATKInstantEffect extends DamageEffect {

    @XmlAttribute
    protected boolean percent;

    @Override
    public void calculate(Effect effect) {
        if (!super.calculate(effect, null, null)) {
            return;
        }

        int valueWithDelta = value + delta * effect.getSkillLevel();
        if (percent) {
            valueWithDelta = (int) (valueWithDelta / 100f * effect.getEffected().getLifeStats().getMaxHp());
        }
        int critAddDmg = critAddDmg2 + critAddDmg1 * effect.getSkillLevel();
        AttackUtil.calculateMagicalSkillResult(effect, valueWithDelta, null, getElement(), false, true, true, getMode(),
            critProbMod2, critAddDmg);
    }
}
