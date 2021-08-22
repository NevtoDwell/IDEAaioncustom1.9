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

public final class AdvCustomConfig {


    @Property(key = "gameserver.quest.repeat.update", defaultValue = "2 0 9 ? * *")
    public static String QUEST_REPEAT_UPDATE;

    /**
     * Cube Size
     */
    @Property(key = "gameserver.cube.size", defaultValue = "0")
    public static int CUBE_SIZE;

    @Property(key = "gameserver.notquest.craftskill", defaultValue = "false")
    public static boolean NOT_CRAFTSKILL_QUEST;

    /**
     * InGameShop Limit
     */
    @Property(key = "gameserver.gameshop.limit", defaultValue = "false")
    public static boolean GAMESHOP_LIMIT;

    @Property(key = "gameserver.gameshop.category", defaultValue = "0")
    public static byte GAMESHOP_CATEGORY;

    @Property(key = "gameserver.gameshop.limit.time", defaultValue = "60")
    public static long GAMESHOP_LIMIT_TIME;

    /**
     * Siege Auto Race
     */
    @Property(key = "gameserver.auto.source.race", defaultValue = "false")
    public static boolean AUTO_SOURCE_RACE;

    @Property(key = "gameserver.auto.source.id", defaultValue = "4011,4021;4031,4041")
    public static String AUTO_SOURCE_LOCID;

    @Property(key = "s", defaultValue = "false")
    public static boolean SIEGE_AUTO_RACE;

    @Property(key = "gameserver.auto.siege.id", defaultValue = "2011,2021;3011,3021")
    public static String SIEGE_AUTO_LOCID;

    @Property(key = "gameserver.craft.delaytime,rate", defaultValue = "2")
    public static Integer CRAFT_DELAYTIME_RATE;
}
