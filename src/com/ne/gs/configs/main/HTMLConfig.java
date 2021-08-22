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
public final class HTMLConfig {

    /**
     * Enable HTML Welcome Message
     */
    @Property(key = "gameserver.html.welcome.enable", defaultValue = "false")
    public static boolean ENABLE_HTML_WELCOME;

    /**
     * Enable HTML Guide Message
     */
    @Property(key = "gameserver.html.guides.enable", defaultValue = "false")
    public static boolean ENABLE_GUIDES;

    /**
     * Give 1 guide per account
     */
    @Property(key = "gameserver.html.guides.account", defaultValue = "false")
    public static boolean ACCOUNT_GUIDES;

    /**
     * Html files directory
     */
    @Property(key = "gameserver.html.root", defaultValue = "./data/static_data/HTML/")
    public static String HTML_ROOT;

    /**
     * Html cache directory
     */
    @Property(key = "gameserver.html.cache.file", defaultValue = "./cache/html.cache")
    public static String HTML_CACHE_FILE;

    /**
     * Encoding
     */
    @Property(key = "gameserver.html.encoding", defaultValue = "UTF-8")
    public static String HTML_ENCODING;
}
