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
import com.ne.gs.model.templates.zone.ZoneInfo;
import com.ne.gs.model.templates.zone.ZoneType;

/**
 * @author MrPoke
 */
public class PvPZoneInstance extends SiegeZoneInstance {

    /**
     * @param mapId
     * @param template
     */
    public PvPZoneInstance(int mapId, ZoneInfo template) {
        super(mapId, template);
    }

    @Override
    public synchronized boolean onEnter(Creature creature) {
        if (super.onEnter(creature)) {
            creature.setInsideZoneType(ZoneType.PVP);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized boolean onLeave(Creature creature) {
        if (super.onLeave(creature)) {
            creature.unsetInsideZoneType(ZoneType.PVP);
            return true;
        } else {
            return false;
        }
    }
}
