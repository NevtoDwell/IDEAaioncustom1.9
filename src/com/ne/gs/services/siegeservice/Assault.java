/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import java.util.concurrent.Future;

import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.siege.SiegeRace;

public abstract class Assault<siege extends Siege<?>> {

    protected final SiegeLocation siegeLocation;
    protected final int locationId;
    protected final SiegeNpc boss;
    protected final int worldId;
    protected Future<?> dredgionTask;
    protected Future<?> spawnTask;

    public Assault(Siege<?> siege) {
        siegeLocation = siege.getSiegeLocation();
        boss = siege.getBoss();
        locationId = siege.getSiegeLocationId();
        worldId = siege.getSiegeLocation().getWorldId();
    }

    public int getWorldId() {
        return worldId;
    }

    public void startAssault(int delay) {
        scheduleAssault(delay);
    }

    public void finishAssault(boolean captured) {
        if (dredgionTask != null && !dredgionTask.isDone()) {
            dredgionTask.cancel(true);
        }
        if (spawnTask != null && !spawnTask.isDone()) {
            spawnTask.cancel(true);
        }
        onAssaultFinish(captured && siegeLocation.getRace().equals(SiegeRace.BALAUR));
    }

    protected abstract void onAssaultFinish(boolean paramBoolean);

    protected abstract void scheduleAssault(int paramInt);
}
