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

import com.ne.gs.controllers.observer.RoadObserver;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.road.Road;

/**
 * @author SheppeR
 */
public class RoadController extends VisibleObjectController<Road> {

    FastMap<Integer, RoadObserver> observed = new FastMap<Integer, RoadObserver>().shared();

    @Override
    public void see(VisibleObject object) {
        Player p = (Player) object;
        RoadObserver observer = new RoadObserver(getOwner(), p);
        p.getObserveController().addObserver(observer);
        observed.put(p.getObjectId(), observer);
    }

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
        Player p = (Player) object;
        RoadObserver observer = observed.remove(p.getObjectId());
        if (isOutOfRange) {
            observer.moved();
        }
        p.getObserveController().removeObserver(observer);
    }
}
