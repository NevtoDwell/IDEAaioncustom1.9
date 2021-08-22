/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.main;

import java.util.regex.Pattern;

import com.ne.commons.configuration.Property;

public final class NameConfig {

    @Property(key = "gameserver.name.characterpattern", defaultValue = "[a-zA-Z]{2,16}")
    public static Pattern CHAR_NAME_PATTERN;

    @Property(key = "gameserver.name.forbidden.sequences", defaultValue = "")
    public static String NAME_SEQUENCE_FORBIDDEN;

    @Property(key = "gameserver.name.forbidden.enable.client", defaultValue = "true")
    public static boolean NAME_FORBIDDEN_ENABLE;

    @Property(key = "gameserver.name.forbidden.client", defaultValue = "")
    public static String NAME_FORBIDDEN_CLIENT;
}
