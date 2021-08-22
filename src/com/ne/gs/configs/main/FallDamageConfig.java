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

public final class FallDamageConfig {

    /**
     * Percentage of damage per meter.
     */
    @Property(key = "gameserver.falldamage.percentage", defaultValue = "1.0")
    public static float FALL_DAMAGE_PERCENTAGE;

    /**
     * Minimum fall damage range
     */
    @Property(key = "gameserver.falldamage.distance.minimum", defaultValue = "10")
    public static int MINIMUM_DISTANCE_DAMAGE;

    /**
     * Maximum fall distance after which you will die after hitting the ground.
     */
    @Property(key = "gameserver.falldamage.distance.maximum", defaultValue = "50")
    public static int MAXIMUM_DISTANCE_DAMAGE;

    /**
     * Maximum fall distance after which you will die in mid air.
     */
    @Property(key = "gameserver.falldamage.distance.midair", defaultValue = "200")
    public static int MAXIMUM_DISTANCE_MIDAIR;
}
