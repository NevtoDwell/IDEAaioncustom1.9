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
 * @author Sarynth, xTz, Source
 */
public final class SiegeConfig {

    /**
     * Siege Enabled
     */
    @Property(key = "gameserver.siege.enable", defaultValue = "true")
    public static boolean SIEGE_ENABLED;

    /**
     * Siege Reward Rate
     */
    @Property(key = "gameserver.siege.medal.rate", defaultValue = "1")
    public static int SIEGE_MEDAL_RATE;
    /**
     * Siege Legion Reward Rate
     */
    @Property(key = "gameserver.siege.legion.medal.rate", defaultValue = "1")
    public static int SIEGE_LEGION_MEDAL_RATE;

    /**
     * Siege sield Enabled
     */
    @Property(key = "gameserver.siege.shield.enable", defaultValue = "true")
    public static boolean SIEGE_SHIELD_ENABLED;
    
    @Property(key = "gameserver.siege.assault.enable", defaultValue = "false")
    public static boolean BALAUR_AUTO_ASSAULT;

    @Property(key = "gameserver.siege.assault.rate", defaultValue = "1")
    public static float BALAUR_ASSAULT_RATE;

    @Property(key = "gameserver.siege.protector.time", defaultValue = "0 0 21 ? * *")
    public static String RACE_PROTECTOR_SPAWN_SCHEDULE;

    @Property(key = "gameserver.sunayaka.time", defaultValue = "0 0 23 ? * *")
    public static String BERSERKER_SUNAYAKA_SPAWN_SCHEDULE;

    @Property(key = "gameserver.sunayaka.governor.respawn.from", defaultValue = "24")
    public static int GOVERNOR_SUNAYAKA_RESPAWN_FROM;

    @Property(key = "gameserver.sunayaka.governor.respawn.to", defaultValue = "30")
    public static int GOVERNOR_SUNAYAKA_RESPAWN_TO;
    
    @Property(key = "gameserver.locations.with.disabled.assault", defaultValue = "0")
    public static String LOCATIONS_WITH_DISABLED_ASSAULT;

    @Property(key = "gameserver.tiamaranta.portals", defaultValue = "false")
    public static boolean ALL_TIME_AVAILABLE_PORTALS;

	@Property(key = "gameserver.moltenus.time", defaultValue = "0 0 22 ? * SUN")
	public static String MOLTENUS_SPAWN_SCHEDULE;
}
