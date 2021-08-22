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
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostileUpEffect")
public class HostileUpEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        Creature effected = effect.getEffected();
        if (effected instanceof Npc) {
            effected.getAggroList().addHate(effect.getEffector(), effect.getTauntHate());
        }
    }

    @Override
    public void calculate(Effect effect) {
        if (!super.calculate(effect, null, null)) {
            return;
        }
        effect.setTauntHate(value + delta * effect.getSkillLevel());
    }
}
