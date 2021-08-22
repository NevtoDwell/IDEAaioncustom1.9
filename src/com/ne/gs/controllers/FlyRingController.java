/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import javolution.util.FastMap;

import com.ne.gs.controllers.observer.FlyRingObserver;
import com.ne.gs.model.flyring.FlyRing;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author xavier
 */
public class FlyRingController extends VisibleObjectController<FlyRing> {

    FastMap<Integer, FlyRingObserver> observed = new FastMap<Integer, FlyRingObserver>().shared();

    @Override
    public void see(VisibleObject object) {
        Player p = (Player) object;
        FlyRingObserver observer = new FlyRingObserver(getOwner(), p);
        p.getObserveController().addObserver(observer);
        observed.put(p.getObjectId(), observer);
    }

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
        Player p = (Player) object;
        FlyRingObserver observer = observed.remove(p.getObjectId());
        if (isOutOfRange) {
            observer.moved();
        }
        p.getObserveController().removeObserver(observer);
    }
}
