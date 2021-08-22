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
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.change.Func;
import com.ne.gs.skillengine.effect.modifier.ActionModifier;
import com.ne.gs.skillengine.model.DispelCategoryType;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTargetSlot;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DispelBuffCounterAtkEffect")
public class DispelBuffCounterAtkEffect extends DamageEffect {

    @XmlAttribute
    protected int dpower;
    @XmlAttribute
    protected int power;
    @XmlAttribute
    protected int hitvalue;
    @XmlAttribute
    protected int hitdelta;
    @XmlAttribute(name = "dispel_level")
    protected int dispelLevel;

    private int i;
    private int finalPower;


    @Override
    public void applyEffect(Effect effect) {
        super.applyEffect(effect);
        effect.getEffected().getEffectController().dispelBuffCounterAtkEffect(i, dispelLevel, finalPower);
    }

    @Override
    public void calculate(Effect effect) {
        if (!super.calculate(effect, null, null))
            return;

        Creature effected = effect.getEffected();
        int count = value + delta * effect.getSkillLevel();
        finalPower = power + dpower * effect.getSkillLevel();

        i = effected.getEffectController().calculateNumberOfEffects(dispelLevel);
        i = (i < count ? i : count);

        int newValue = 0;
        if (i == 1)
            newValue = hitvalue;
        else if (i > 1)
            newValue = hitvalue + ((hitvalue / 2) * (i - 1));

        int valueWithDelta = newValue + hitdelta * effect.getSkillLevel();

        ActionModifier modifier = getActionModifiers(effect);

        AttackUtil.calculateMagicalSkillResult(effect, valueWithDelta, modifier, getElement());
    }
}
