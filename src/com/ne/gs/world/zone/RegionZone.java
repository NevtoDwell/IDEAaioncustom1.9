/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone;

import com.ne.gs.configs.main.WorldConfig;
import com.ne.gs.model.geometry.AbstractArea;
import com.ne.gs.model.geometry.RectangleArea;

/**
 * @author ATracer
 */
public class RegionZone extends RectangleArea {

    public RegionZone(float startX, float startY, float minZ, float maxZ) {
        super(null, 0, startX, startY, startX + WorldConfig.WORLD_REGION_SIZE, startY + WorldConfig.WORLD_REGION_SIZE, minZ, maxZ);
    }

    public boolean isInside(AbstractArea area) {
        return true;
    }
}
