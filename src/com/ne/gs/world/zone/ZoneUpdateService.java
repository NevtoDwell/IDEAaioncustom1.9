/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.taskmanager.AbstractFIFOPeriodicTaskManager;

/**
 * @author ATracer
 */
public class ZoneUpdateService extends AbstractFIFOPeriodicTaskManager<Creature> {

    private ZoneUpdateService() {
        super(500);
    }

    @Override
    protected void callTask(Creature creature) {
        creature.getController().refreshZoneImpl();
        if (creature instanceof Player) {
            ZoneLevelService.checkZoneLevels((Player) creature);
        }
    }

    @Override
    protected String getCalledMethodName() {
        return "ZoneUpdateService()";
    }

    public static ZoneUpdateService getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final ZoneUpdateService instance = new ZoneUpdateService();
    }

}
