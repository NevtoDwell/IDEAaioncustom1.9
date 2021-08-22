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
import com.ne.gs.model.templates.zone.ZoneInfo;
import com.ne.gs.model.templates.zone.ZoneType;

/**
 * @author hex1r0
 */
public class NeutralZoneInstance extends ZoneInstance {

    public NeutralZoneInstance(int mapId, ZoneInfo template) {
        super(mapId, template);
    }

    @Override
    public synchronized boolean onEnter(Creature creature) {
        if (creature instanceof Player) {
            if (super.onEnter(creature)) {
                creature.setInsideZoneType(ZoneType.NEUTRAL);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean onLeave(Creature creature) {
        if (creature instanceof Player) {
            if (super.onLeave(creature)) {
                creature.unsetInsideZoneType(ZoneType.NEUTRAL);
                return true;
            }
        }
        return false;
    }
}
