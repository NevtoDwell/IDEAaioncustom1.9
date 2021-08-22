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
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.stats.calc.functions.StatShieldMasteryFunction;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author VladimirZ
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShieldMasteryEffect")
public class ShieldMasteryEffect extends BufEffect {

    @Override
    public void startEffect(Effect effect) {

        List<IStatFunction> modifiers = getModifiers(effect);
        List<IStatFunction> masteryModifiers = new ArrayList<>(modifiers.size());
        for (IStatFunction modifier : modifiers) {
            masteryModifiers.add(new StatShieldMasteryFunction(modifier.getName(), modifier.getValue(), modifier.isBonus()));
        }
        if (masteryModifiers.size() > 0) {
            effect.getEffected().getGameStats().addEffect(effect, masteryModifiers);
        }
    }
}
