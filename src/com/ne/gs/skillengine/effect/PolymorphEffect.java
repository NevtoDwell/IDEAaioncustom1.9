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

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolymorphEffect")
public class PolymorphEffect extends TransformEffect {

    @Override
    public void startEffect(Effect effect) {
        if (model > 0) {
            Creature effected = effect.getEffected();
            NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(model);
            if (template != null) {
                effected.getTransformModel().setTribe(template.getTribe(), false);
            }
        }
        super.startEffect(effect, AbnormalState.NOFLY);
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect, AbnormalState.NOFLY);
        effect.getEffected().getTransformModel().setActive(false);
    }
}
