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
 * @author xTz
 */
public final class InGameShopConfig {

    /**
     * Enable in game shop
     */
    @Property(key = "gameserver.ingameshop.enable", defaultValue = "false")
    public static boolean ENABLE_IN_GAME_SHOP;

    /**
     * Enable gift system between factions
     */
    @Property(key = "gameserver.ingameshop.gift", defaultValue = "false")
    public static boolean ENABLE_GIFT_OTHER_RACE;

    @Property(key = "gameserver.ingameshop.allow.gift", defaultValue = "true")
    public static boolean ALLOW_GIFTS;
}
