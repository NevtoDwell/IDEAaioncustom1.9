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
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChangeHateOnAttackedEffect")
public class ChangeHateOnAttackedEffect extends EffectTemplate {

    @XmlAttribute
    protected int value1;// delta
    @XmlAttribute
    protected int value2;

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void startEffect(final Effect effect) {
        super.startEffect(effect);

        // TODO: maybe this isn't correct formula?
        final int finalValue = value1 + value2;

        ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {

            @Override
            public void attacked(Creature creature) {
                if (creature instanceof Npc) {
                    creature.getAggroList().addHate(effect.getEffected(), finalValue);
                }
            }
        };

        effect.getEffected().getObserveController().addObserver(observer);
        effect.setActionObserver(observer, position);
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect);
        ActionObserver observer = effect.getActionObserver(position);
        if (observer != null) {
            effect.getEffected().getObserveController().removeObserver(observer);
        }
    }
}
