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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_RESURRECT;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResurrectPositionalEffect")
public class ResurrectPositionalEffect extends ResurrectEffect {

    @Override
    public void applyEffect(Effect effect) {
        Player effector = (Player) effect.getEffector();
        Player effected = (Player) effect.getEffected();

        effected.setPlayerResActivate(true);
        effected.setResurrectionSkill(skillId);
        effected.sendPck(new SM_RESURRECT(effect.getEffector(), effect.getSkillId()));
        effected.setResPosState(true);
        effected.setResPosX(effector.getX());
        effected.setResPosY(effector.getY());
        effected.setResPosZ(effector.getZ());
    }

    @Override
    public void calculate(Effect effect) {
        if ((effect.getEffector() instanceof Player) && (effect.getEffected() instanceof Player) && (effect.getEffected().getLifeStats().isAlreadyDead())) {
            super.calculate(effect, null, null);
        }
    }
}
