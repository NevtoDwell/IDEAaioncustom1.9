/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTemplate;

public class DelayedSkillEffect extends EffectTemplate {

    @XmlAttribute(name = "skill_id")
    protected int skillId;

    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    public void endEffect(Effect effect) {
        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
        Effect e = new Effect(effect.getEffector(), effect.getEffected(), template, template.getLvl(), 0);
        e.initialize();
        e.applyEffect();
    }
}
