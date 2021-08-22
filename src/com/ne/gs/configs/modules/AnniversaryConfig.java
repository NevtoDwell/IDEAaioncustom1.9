/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.modules;

import com.ne.commons.configuration.Property;

/**
 * @author hex1r0
 */
public final class AnniversaryConfig {

    /**
     * Returners campaign (players returned after some time being offline)
     */
    @Property(key = "modules.anniversary.returners", defaultValue = "false")
    public static boolean RETURNERS_ENABLED;

    /**
     * Player days offline required for bonus to apply
     */
    @Property(key = "modules.anniversary.returners.daysoffline", defaultValue = "60")
    public static int RETURNERS_DAYSOFFLINE;

    /**
     * Start time of campaign
     */
    @Property(key = "modules.anniversary.returners.start", defaultValue = "2013-01-23")
    public static String RETURNERS_START;

    /**
     * End time of campaign
     */
    @Property(key = "modules.anniversary.returners.end", defaultValue = "2013-01-24")
    public static String RETURNERS_END;

    /**
     * Number of coints for returner
     */
    @Property(key = "modules.anniversary.returners.coins", defaultValue = "200")
    public static int RETURNERS_COINS;
}
