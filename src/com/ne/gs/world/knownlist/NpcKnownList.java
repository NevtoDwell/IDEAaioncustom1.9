/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.knownlist;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.world.MapRegion;

/**
 * @author ATracer
 */
public class NpcKnownList extends CreatureAwareKnownList {

    public NpcKnownList(VisibleObject owner) {
        super(owner);
    }

    @Override
    public void doUpdate() {
        MapRegion activeRegion = owner.getActiveRegion();
        if (activeRegion != null && activeRegion.isMapRegionActive()) {
            super.doUpdate();
        } else {
            clear();
        }
    }
}
