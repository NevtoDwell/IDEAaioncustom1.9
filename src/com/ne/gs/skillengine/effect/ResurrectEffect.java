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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_RESURRECT;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResurrectEffect")
public class ResurrectEffect extends EffectTemplate {

    @XmlAttribute(name = "skill_id")
    protected int skillId;

    @Override
    public void applyEffect(Effect effect) {
        Player effectedPlayer = (Player) effect.getEffected();
        effectedPlayer.setPlayerResActivate(true);
        effectedPlayer.setResurrectionSkill(skillId);
        effectedPlayer.sendPck(new SM_RESURRECT(effect.getEffector(), effect.getSkillId()));
    }

    @Override
    public void calculate(Effect effect) {
        if (effect.getEffected() instanceof Player && effect.getEffected().getLifeStats().isAlreadyDead()) {
            super.calculate(effect, null, null);
        }
    }
}
