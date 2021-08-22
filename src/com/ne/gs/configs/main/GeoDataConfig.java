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

public final class GeoDataConfig {

    /**
     * Enable Fear skill using geodata.
     */
    @Property(key = "gameserver.geodata.fear.enable", defaultValue = "true")
    public static boolean FEAR_ENABLE;

    /**
     * Show collision zone name and skill id
     */
    @Property(key = "gameserver.geo.materials.showdetails", defaultValue = "false")
    public static boolean GEO_MATERIALS_SHOWDETAILS;

    /**
     * Enable geo shields
     */
    @Property(key = "gameserver.geo.shields.enable", defaultValue = "false")
    public static boolean GEO_SHIELDS_ENABLE;
}
