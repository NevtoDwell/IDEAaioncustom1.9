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

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author Rama and Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoostSkillCostEffect")
public class BoostSkillCostEffect extends BufEffect {

    @XmlAttribute
    protected boolean percent;

    @Override
    public void startEffect(Effect effect) {
        super.startEffect(effect);

        ActionObserver observer = new ActionObserver(ObserverType.SKILLUSE) {

            @Override
            public void skilluse(Skill skill) {
                skill.setBoostSkillCost(value);
            }
        };

        effect.getEffected().getObserveController().addObserver(observer);
        effect.setActionObserver(observer, position);
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect);
        ActionObserver observer = effect.getActionObserver(position);
        effect.getEffected().getObserveController().removeObserver(observer);
    }
}
