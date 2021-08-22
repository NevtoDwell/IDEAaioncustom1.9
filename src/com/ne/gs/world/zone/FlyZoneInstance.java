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
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.templates.zone.ZoneInfo;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author MrPoke
 */
public class FlyZoneInstance extends ZoneInstance {

    public FlyZoneInstance(int mapId, ZoneInfo template) {
        super(mapId, template);
    }

    @Override
    public synchronized boolean onEnter(Creature creature) {
        if (super.onEnter(creature)) {
            creature.setInsideZoneType(ZoneType.FLY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized boolean onLeave(Creature creature) {
        if (super.onLeave(creature)) {
            creature.unsetInsideZoneType(ZoneType.FLY);
            if (creature.isInState(CreatureState.FLYING) && !creature.isInState(CreatureState.FLIGHT_TELEPORT)) {
                if (creature instanceof Player) {
                    AuditLogger.info((Player) creature, "On leave Fly zone in fly state!!");
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
