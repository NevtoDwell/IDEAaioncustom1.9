/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.main;

import java.util.Calendar;

import com.ne.commons.configuration.Property;

public final class GSConfig {

    @Property(key = "gameserver.version", defaultValue = "3.0.0.0")
    public static String SERVER_VERSION;

    /**
     * Server Country Code
     */
    @Property(key = "gameserver.country.code", defaultValue = "1")
    public static int SERVER_COUNTRY_CODE;

    @Property(key = "gameserver.name", defaultValue = "Aion")
    public static String SERVER_NAME;
    @Property(key = "gameserver.players.max.level", defaultValue = "60")
    public static int PLAYER_MAX_LEVEL;

    /**
     * Time Zone name (used for event config atm)
     */
    @Property(key = "gameserver.timezone", defaultValue = "")
    public static String TIME_ZONE_ID = Calendar.getInstance().getTimeZone().getID();

    /**
     * Enable chat server connection
     */
    @Property(key = "gameserver.chatserver.enable", defaultValue = "false")
    public static boolean ENABLE_CHAT_SERVER;

    @Property(key = "gameserver.character.creation.mode", defaultValue = "0")
    public static int CHARACTER_CREATION_MODE;
    /**
     * Server Mode
     */
    @Property(key = "gameserver.character.limit.count", defaultValue = "8")
    public static int CHARACTER_LIMIT_COUNT;

    @Property(key = "gameserver.character.faction.limitation.mode", defaultValue = "0")
    public static int CHARACTER_FACTION_LIMITATION_MODE;

    @Property(key = "gameserver.ratio.limitation.enable", defaultValue = "false")
    public static boolean ENABLE_RATIO_LIMITATION;

    @Property(key = "gameserver.ratio.min.value", defaultValue = "60")
    public static int RATIO_MIN_VALUE;

    @Property(key = "gameserver.ratio.min.required.level", defaultValue = "10")
    public static int RATIO_MIN_REQUIRED_LEVEL;

    @Property(key = "gameserver.ratio.min.characters_count", defaultValue = "50")
    public static int RATIO_MIN_CHARACTERS_COUNT;

    @Property(key = "gameserver.ratio.high_player_count.disabling", defaultValue = "500")
    public static int RATIO_HIGH_PLAYER_COUNT_DISABLING;

    @Property(key = "gameserver.abyssranking.small.cache", defaultValue = "false")
    public static boolean ABYSSRANKING_SMALL_CACHE;

    @Property(key = "gameserver.character.reentry.time", defaultValue = "20")
    public static int CHARACTER_REENTRY_TIME;

}
