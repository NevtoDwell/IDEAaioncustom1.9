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
 * @author synchro2
 */
public final class PunishmentConfig {

    @Property(key = "gameserver.punishment.enable", defaultValue = "false")
    public static boolean PUNISHMENT_ENABLE;

    @Property(key = "gameserver.punishment.type", defaultValue = "1")
    public static int PUNISHMENT_TYPE;

    @Property(key = "gameserver.punishment.time", defaultValue = "1440")
    public static int PUNISHMENT_TIME;
    @Property(key = "gameserver.punishment.reduceap", defaultValue = "0")
    public static int PUNISHMENT_REDUCEAP;
}
