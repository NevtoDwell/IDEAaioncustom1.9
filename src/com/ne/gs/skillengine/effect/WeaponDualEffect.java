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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.stats.calc.functions.StatDualWeaponMasteryFunction;
import com.ne.gs.skillengine.model.Effect;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeaponDualEffect")
public class WeaponDualEffect extends BufEffect {

    @Override
    public void startEffect(Effect effect) {
        if (change == null) {
            return;
        }

        if (effect.getEffected() instanceof Player) {
            ((Player) effect.getEffected()).setDualEffectValue(value);
        }

        List<IStatFunction> modifiers = getModifiers(effect);
        List<IStatFunction> masteryModifiers = new ArrayList<>(modifiers.size());
        for (IStatFunction modifier : modifiers) {
            masteryModifiers.add(new StatDualWeaponMasteryFunction(effect, modifier));
        }
        if (masteryModifiers.size() > 0) {
            effect.getEffected().getGameStats().addEffect(effect, masteryModifiers);
        }
    }

    @Override
    public void endEffect(Effect effect) {
        if (effect.getEffected() instanceof Player) {
            ((Player) effect.getEffected()).setDualEffectValue(0);
        }

        super.endEffect(effect);
    }

}
