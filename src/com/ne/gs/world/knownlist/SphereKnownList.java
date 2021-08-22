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
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer
 */
public class SphereKnownList extends PlayerAwareKnownList {

    private final float radius;

    public SphereKnownList(VisibleObject owner, float radius) {
        super(owner);
        this.radius = radius;
    }

    @Override
    protected boolean checkReversedObjectInRange(VisibleObject newObject) {
        return MathUtil.isIn3dRange(owner, newObject, radius);
    }
}
