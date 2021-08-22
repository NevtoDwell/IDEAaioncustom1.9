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
 * @author Hilgert
 */
public final class DredgionConfig {

    @Property(key = "gameserver.dredgion.timer", defaultValue = "120")
    public static long DREDGION_TIMER;

    @Property(key = "gameserver.dredgion2.enable", defaultValue = "true")
    public static boolean DREDGION2_ENABLE;

    @Property(key = "gameserver.dredgion.time", defaultValue = "0 0 0,12,20 ? * *")
    public static String DREDGION_TIMES;

    @Property(key = "gameserver.dredgion.minteamsize", defaultValue = "3")
    public static int DREDGION_MIN_TEAM_SIZE;

    @Property(key = "gameserver.dredgion.winner.points.dred1", defaultValue = "3000")
    public static int WINNER_POINTS_DRED1;

    @Property(key = "gameserver.dredgion.winner.points.dred2", defaultValue = "4500")
    public static int WINNER_POINTS_DRED2;

    @Property(key = "gameserver.dredgion.winner.points.dred3", defaultValue = "6500")
    public static int WINNER_POINTS_DRED3;

    @Property(key = "gameserver.dredgion.looser.points.dred1", defaultValue = "1500")
    public static int LOOSER_POINTS_DRED1;

    @Property(key = "gameserver.dredgion.looser.points.dred2", defaultValue = "2500")
    public static int LOOSER_POINTS_DRED2;

    @Property(key = "gameserver.dredgion.looser.points.dred3", defaultValue = "4000")
    public static int LOOSER_POINTS_DRED3;

    @Property(key = "gameserver.dredgion.draw.points.dred1", defaultValue = "2250")
    public static int DRAW_POINTS_DRED1;

    @Property(key = "gameserver.dredgion.draw.points.dred2", defaultValue = "3750")
    public static int DRAW_POINTS_DRED2;

    @Property(key = "gameserver.dredgion.draw.points.dred3", defaultValue = "5000")
    public static int DRAW_POINTS_DRED3;
}
