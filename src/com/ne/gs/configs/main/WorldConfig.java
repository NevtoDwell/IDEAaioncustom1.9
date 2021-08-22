/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.main;

import com.ne.commons.configuration.Property;

/**
 * @author ATracer
 */
public final class WorldConfig {

    /**
     * World region size
     */
    @Property(key = "gameserver.world.region.size", defaultValue = "128")
    public static int WORLD_REGION_SIZE;

    /**
     * Trace active regions and deactivate inactive
     */
    @Property(key = "gameserver.world.region.active.trace", defaultValue = "true")
    public static boolean WORLD_ACTIVE_TRACE;
}
