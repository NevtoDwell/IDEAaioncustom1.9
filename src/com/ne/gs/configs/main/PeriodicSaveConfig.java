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
public final class PeriodicSaveConfig {

    /**
     * Time in seconds for saving player data
     */
    @Property(key = "gameserver.periodicsave.player.general", defaultValue = "900")
    public static int PLAYER_GENERAL;

    /**
     * Time in seconds for saving player items and item stones
     */
    @Property(key = "gameserver.periodicsave.player.items", defaultValue = "900")
    public static int PLAYER_ITEMS;

    /**
     * Time in seconds for saving legion wh items and item stones
     */
    @Property(key = "gameserver.periodicsave.legion.items", defaultValue = "1200")
    public static int LEGION_ITEMS;

    /**
     * Time in seconds for saving broker
     */
    @Property(key = "gameserver.periodicsave.broker", defaultValue = "1500")
    public static int BROKER;

    @Property(key = "gameserver.periodicsave.player.pets", defaultValue = "5")
    public static int PLAYER_PETS;

}
