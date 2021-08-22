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
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.action.DamageType;
import com.ne.gs.skillengine.model.DashStatus;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.world.World;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DashEffect")
public class DashEffect extends DamageEffect {

    @Override
    public void applyEffect(Effect effect) {
        super.applyEffect(effect);
        Player effector = (Player) effect.getEffector();

        // Move Effector to Effected
        Skill skill = effect.getSkill();
        World.getInstance().updatePosition(effector, skill.getX(), skill.getY(), skill.getZ(), skill.getH());
    }

    @Override
    public void calculate(Effect effect) {
        if (effect.getEffected() == null) {
            return;
        }

        if (!(effect.getEffector() instanceof Player)) {
            return;
        }

        if (!super.calculate(effect, DamageType.PHYSICAL)) {
            return;
        }

        Creature effected = effect.getEffected();
        effect.setDashStatus(DashStatus.DASH);
        effect.getSkill().setTargetPosition(effected.getX(), effected.getY(), effected.getZ(), effected.getHeading());
    }
}
