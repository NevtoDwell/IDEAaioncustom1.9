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
 * @author lord_rex
 */
public final class ShutdownConfig {

    /**
     * Shutdown Hook Mode.
     */
    @Property(key = "gameserver.shutdown.mode", defaultValue = "1")
    public static int HOOK_MODE;

    /**
     * Shutdown Hook delay.
     */
    @Property(key = "gameserver.shutdown.delay", defaultValue = "60")
    public static int HOOK_DELAY;

    /**
     * Shutdown announce interval.
     */
    @Property(key = "gameserver.shutdown.interval", defaultValue = "1")
    public static int ANNOUNCE_INTERVAL;

    /**
     * Safe reboot mode.
     */
    @Property(key = "gameserver.shutdown.safereboot", defaultValue = "true")
    public static boolean SAFE_REBOOT;
}
