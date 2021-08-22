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

import com.ne.commons.func.tuple.Tuple;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.handlers.EffectResurrectBaseHandler;
import com.ne.gs.skillengine.model.Effect;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResurrectBaseEffect")
public class ResurrectBaseEffect extends ResurrectEffect {

    public void calculate(Effect effect) {
        calculate(effect, null, null);
    }

    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void startEffect(final Effect effect) {
        Creature effected = effect.getEffected();

        if (effected instanceof Player) {
            ActionObserver observer = new ActionObserver(ObserverType.DEATH) {
                @Override
                public void died(Creature creature) {
                    if (creature instanceof Player) {
                        Player effected = (Player) effect.getEffected();
                        effected.getChainer().handle(EffectResurrectBaseHandler.class, Tuple.of(effected, skillId));
                    }
                }
            };
            effect.getEffected().getObserveController().attach(observer);
            effect.setActionObserver(observer, position);
        }
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect);

        if (!effect.getEffected().getLifeStats().isAlreadyDead() && effect.getActionObserver(position) != null) {
            effect.getEffected().getObserveController().removeObserver(effect.getActionObserver(position));
        }
    }
}
