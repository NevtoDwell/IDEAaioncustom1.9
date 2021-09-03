/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.administration;

import com.ne.commons.configuration.Property;

/**
 * @author ATracer
 */
public final class AdminConfig {

    /**
     * Admin properties
     */
    @Property(key = "gameserver.administration.gmlevel", defaultValue = "3")
    public static int GM_LEVEL;
    @Property(key = "gameserver.administration.flight.unlimited", defaultValue = "3")
    public static int GM_FLIGHT_UNLIMITED;
    @Property(key = "gameserver.administration.doors.opening", defaultValue = "3")
    public static int DOORS_OPEN;
    @Property(key = "gameserver.administration.auto.res", defaultValue = "3")
    public static int ADMIN_AUTO_RES;
    @Property(key = "gameserver.administration.instancereq", defaultValue = "3")
    public static int INSTANCE_REQ;
    @Property(key = "gameserver.administration.view.player", defaultValue = "3")
    public static int ADMIN_VIEW_DETAILS;

    /**
     * Admin options
     */
    @Property(key = "gameserver.administration.invis.gm.connection", defaultValue = "false")
    public static boolean INVISIBLE_GM_CONNECTION;
    @Property(key = "gameserver.administration.invul.gm.connection", defaultValue = "false")
    public static boolean INVULNERABLE_GM_CONNECTION;
    @Property(key = "gameserver.administration.vision.gm.connection", defaultValue = "false")
    public static boolean VISION_GM_CONNECTION;
    @Property(key = "gameserver.administration.whisper.gm.connection", defaultValue = "false")
    public static boolean WHISPER_GM_CONNECTION;
    @Property(key = "gameserver.administration.quest.dialog.log", defaultValue = "false")
    public static boolean QUEST_DIALOG_LOG;
    
    /**
     * Custom TAG based on access level
     */

    @Property(key = "gameserver.admin.announce.levels", defaultValue = "*")
    public static String ANNOUNCE_LEVEL_LIST;
}
