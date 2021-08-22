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

import com.ne.gs.controllers.observer.ShieldObserver;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.shield.Shield;
import com.ne.gs.model.siege.FortressLocation;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.services.SiegeService;

/**
 * @author Source
 */
public class ShieldController extends VisibleObjectController<Shield> {

    FastMap<Integer, ShieldObserver> observed = new FastMap<Integer, ShieldObserver>().shared();

    @Override
    public void see(VisibleObject object) {
        FortressLocation loc = SiegeService.getInstance().getFortress(getOwner().getId());
        Player player = (Player) object;

        if (loc.isUnderShield()) {
            if (loc.getRace() != SiegeRace.getByRace(player.getRace())) {
                ShieldObserver observer = new ShieldObserver(getOwner(), player);
                player.getObserveController().addObserver(observer);
                observed.put(player.getObjectId(), observer);
            }
        }
    }

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
        FortressLocation loc = SiegeService.getInstance().getFortress(getOwner().getId());
        Player player = (Player) object;

        if (loc.isUnderShield()) {
            if (loc.getRace() != SiegeRace.getByRace(player.getRace())) {
                ShieldObserver observer = observed.remove(player.getObjectId());
                if (observer != null) {
                    if (isOutOfRange) {
                        observer.moved();
                    }

                    player.getObserveController().removeObserver(observer);
                }
            }
        }
    }

}
