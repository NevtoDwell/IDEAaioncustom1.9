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
public final class AbyssRankConfig {

    /**
     * Offliners campaign (remove player from top, if player was offline for a while)
     */
    @Property(key = "modules.abyssrank.offliners", defaultValue = "false")
    public static boolean OFFLINERS;

    /**
     * Timeout (in days) after which player will be removed from the top
     */
    @Property(key = "modules.abyssrank.offliners.daysoffline", defaultValue = "7")
    public static int OFFLINERS_DAYS;
}
