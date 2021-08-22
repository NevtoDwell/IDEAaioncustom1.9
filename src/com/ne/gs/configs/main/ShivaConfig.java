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

public final class ShivaConfig {

    @Property(key = "gameserver.custom.tag.imaginary", defaultValue = "")
    public static String TAG_IMAGINARY;

    @Property(key = "gameserver.custom.tag.naknice", defaultValue = "")
    public static String TAG_NAKNICE;

    @Property(key = "gameserver.custom.tag.carmel", defaultValue = "")
    public static String TAG_CARMEL;

    @Property(key = "gameserver.custom.tag.elissa", defaultValue = "")
    public static String TAG_ELISSA;

    @Property(key = "gameserver.starting.level", defaultValue = "1")
    public static int STARTING_LEVEL;

    @Property(key = "gameserver.startClass.maxLevel", defaultValue = "10")
    public static int STARTCLASS_MAXLEVEL;
}
