/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.attack;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class PlayerAggroList extends AggroList {

    /**
     * @param owner
     */
    public PlayerAggroList(Creature owner) {
        super(owner);
    }

    @Override
    protected boolean isAware(Creature creature) {
        return creature instanceof Player && !creature.getObjectId().equals(owner.getObjectId());
    }
}
