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

public final class CleaningConfig {

    @Property(key = "gameserver.cleaning.enable", defaultValue = "false")
    public static boolean CLEANING_ENABLE;

    @Property(key = "gameserver.cleaning.period", defaultValue = "180")
    public static int CLEANING_PERIOD;

    @Property(key = "gameserver.cleaning.threads", defaultValue = "2")
    public static int CLEANING_THREADS;

    @Property(key = "gameserver.cleaning.limit", defaultValue = "5000")
    public static int CLEANING_LIMIT;
}
