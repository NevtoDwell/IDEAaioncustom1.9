/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.periodicaction;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author antness
 */
public class MpUsePeriodicAction extends PeriodicAction {

    @XmlAttribute(name = "value")
    protected int value;

    @Override
    public void act(Effect effect) {
        Creature effected = effect.getEffected();
        int maxMp = effected.getGameStats().getMaxMp().getCurrent();
        int requiredMp = (int) (maxMp * (value / 100f));
        if (effected.getLifeStats().getCurrentMp() < requiredMp) {
            effect.endEffect();
            return;
        }
        effected.getLifeStats().reduceMp(requiredMp);
    }

}
