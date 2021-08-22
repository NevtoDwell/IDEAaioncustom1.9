/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone.handler;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.world.zone.ZoneInstance;

/**
 * @author MrPoke
 */
public interface AdvencedZoneHandler extends ZoneHandler {

    /**
     * This call if creature die in zone.
     *
     * @param attacker
     * @param target
     *
     * @return TRUE if hadle die event.
     */
    public boolean onDie(Creature attacker, Creature target, ZoneInstance zone);

}
