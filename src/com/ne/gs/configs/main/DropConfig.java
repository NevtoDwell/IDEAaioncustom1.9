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
 * @author Tiger0319
 */
public final class DropConfig {

    /**
     * Disable drop rate reduction based on level diference between players and mobs
     */
    @Property(key = "gameserver.drop.reduction.disable", defaultValue = "false")
    public static boolean DISABLE_DROP_REDUCTION;

    @Property(key = "gameserver.drop.reduction", defaultValue = "8:79;9:39;10:0")
    public static String DROP_REDUCTION;

    /**
     * Enable announce when a player drops Unique / Epic item
     */
    @Property(key = "gameserver.unique.drop.announce.enable", defaultValue = "true")
    public static boolean ENABLE_UNIQUE_DROP_ANNOUNCE;

    /**
     * Force "use_category" for all drops
     */
    @Property(key = "gameserver.drop.forceusecategory", defaultValue = "true")
    public static boolean FORCE_USE_CATEGORY;

    /**
     * Probability to select more than one item per one group
     */
    @Property(key = "gameserver.drop.experimentalmod", defaultValue = "0")
    public static int EXPERIMENTAL_MODIFIER;

    @Property(key = "gameserver.drop.max_lootlist_size", defaultValue = "40")
    public static int MAX_LOOTLIST_SIZE;
}
