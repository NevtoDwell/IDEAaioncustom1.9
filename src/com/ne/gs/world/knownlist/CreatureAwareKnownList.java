/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.knownlist;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;

/**
 * @author ATracer
 */
public class CreatureAwareKnownList extends KnownList {

    public CreatureAwareKnownList(VisibleObject owner) {
        super(owner);
    }

    @Override
    protected final boolean isAwareOf(VisibleObject newObject) {
        return newObject instanceof Creature;
    }
}
