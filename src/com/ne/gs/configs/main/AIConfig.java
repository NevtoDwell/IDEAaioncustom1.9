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
 * @author ATracer
 */
public final class AIConfig {

    /**
     * Debug (for developers)
     */
    @Property(key = "gameserver.ai.move.debug", defaultValue = "true")
    public static boolean MOVE_DEBUG;

    @Property(key = "gameserver.ai.event.debug", defaultValue = "false")
    public static boolean EVENT_DEBUG;

    @Property(key = "gameserver.ai.oncreate.debug", defaultValue = "false")
    public static boolean ONCREATE_DEBUG;

    /**
     * Enable NPC movement
     */
    @Property(key = "gameserver.npcmovement.enable", defaultValue = "true")
    public static boolean ACTIVE_NPC_MOVEMENT;

    /**
     * Minimum movement delay
     */
    @Property(key = "gameserver.npcmovement.delay.minimum", defaultValue = "3")
    public static int MINIMIMUM_DELAY;

    /**
     * Maximum movement delay
     */
    @Property(key = "gameserver.npcmovement.delay.maximum", defaultValue = "15")
    public static int MAXIMUM_DELAY;

    /**
     * Npc Shouts activator
     */
    @Property(key = "gameserver.npcshouts.enable", defaultValue = "true")
    public static boolean SHOUTS_ENABLE;


    /**
     * Gold fountains
     */
    @Property(key = "gameserver.gold.fountain.enable", defaultValue = "True")
    public static boolean GOLD_FOUNTAIN_ENABLE;
    /**
     * Chance to give medal
     */
    @Property(key = "gameserver.gold.fountain.medal.success.chance", defaultValue = "40")
    public static int GOLD_FOUNTAIN_SUCCESS_CHANCE;
    /**
     * Exp reward
     */
    @Property(key = "gameserver.gold.fountain.exp.reward", defaultValue = "2000")
    public static int GOLD_FOUNTAIN_EXP_REWARD;
    /**
     * Gelkmaros and Inggison coin fountains
     */
    @Property(key = "gameserver.platinum.fountain.enable", defaultValue = "True")
    public static boolean PLATINUM_FOUNTAIN_ENABLE;
    /**
     * Chance to give platinum medal
     */
    @Property(key = "gameserver.platinum.fountain.medal.success.chance", defaultValue = "15")
    public static int PLATINUM_FOUNTAIN_SUCCESS_CHANCE;
    /**
     * Exp reward
     */
    @Property(key = "gameserver.platinum.fountain.exp.reward", defaultValue = "1043900")
    public static int PLATINUM_FOUNTAIN_EXP_REWARD;
    /**
     * Mithril fountains
     */
    @Property(key = "gameserver.mithril.fountain.enable", defaultValue = "True")
    public static boolean MITHRIL_FOUNTAIN_ENABLE;
    /**
     * Chance to give medal
     */
    @Property(key = "gameserver.mithril.fountain.medal.success.chance", defaultValue = "20")
    public static int MITHRIL_FOUNTAIN_SUCCESS_CHANCE;
}
