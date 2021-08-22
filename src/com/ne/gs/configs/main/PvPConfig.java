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

public final class PvPConfig {

    @Property(key = "gameserver.pvp.chainkill.time.restriction", defaultValue = "0")
    public static int CHAIN_KILL_TIME_RESTRICTION;

    @Property(key = "gameserver.pvp.chainkill.number.restriction", defaultValue = "30")
    public static int CHAIN_KILL_NUMBER_RESTRICTION;

    @Property(key = "quest.pvp.chainkill.time.restriction", defaultValue = "0")
    public static int QUEST_CHAIN_KILL_TIME_RESTRICTION;

    @Property(key = "quest.pvp.chainkill.number.restriction", defaultValue = "30")
    public static int QUEST_CHAIN_KILL_NUMBER_RESTRICTION;

    @Property(key = "gameserver.pvp.max.leveldiff.restriction", defaultValue = "9")
    public static int MAX_AUTHORIZED_LEVEL_DIFF;

    @Property(key = "gameserver.pvp.medal.rewarding.enable", defaultValue = "false")
    public static boolean ENABLE_MEDAL_REWARDING;

    @Property(key = "gameserver.pvp.medal.reward.chance", defaultValue = "10")
    public static float MEDAL_REWARD_CHANCE;

    @Property(key = "gameserver.pvp.medal.reward.quantity", defaultValue = "1")
    public static int MEDAL_REWARD_QUANTITY;

    //Medal id for levels.
    //lvl 10-45 | Default: 186000031
    @Property(key = "gameserver.pvp.medal.reward.lv45", defaultValue = "186000031")
    public static int MEDAL_REWARD_ID_LV45;
    //lvl 45-50 | Default: 186000030
    @Property(key = "gameserver.pvp.medal.reward.lv50", defaultValue = "186000030")
    public static int MEDAL_REWARD_ID_LV50;
    //lvl 50-55 | Default: 186000096
    @Property(key = "gameserver.pvp.medal.reward.lv55", defaultValue = "186000096")
    public static int MEDAL_REWARD_ID_LV55;
    //lvl 55-60 | Default: 186000147
    @Property(key = "gameserver.pvp.medal.reward.lv60", defaultValue = "186000147")
    public static int MEDAL_REWARD_ID_LV60;

    @Property(key = "gameserver.pvp.toll.rewarding.enable", defaultValue = "false")
    public static boolean ENABLE_TOLL_REWARDING;

    @Property(key = "gameserver.pvp.toll.reward.chance", defaultValue = "50")
    public static float TOLL_REWARD_CHANCE;

    @Property(key = "gameserver.pvp.toll.reward.quantity", defaultValue = "1")
    public static int TOLL_REWARD_QUANTITY;

    @Property(key = "gameserver.pvp.killingspree.enable", defaultValue = "false")
    public static boolean ENABLE_KILLING_SPREE_SYSTEM;

    @Property(key = "gameserver.pvp.raw.killcount.spree", defaultValue = "20")
    public static int SPREE_KILL_COUNT;

    @Property(key = "gameserver.pvp.raw.killcount.rampage", defaultValue = "35")
    public static int RAMPAGE_KILL_COUNT;

    @Property(key = "gameserver.pvp.raw.killcount.genocide", defaultValue = "50")
    public static int GENOCIDE_KILL_COUNT;

    @Property(key = "gameserver.pvp.special_reward.type", defaultValue = "0")
    public static int GENOCIDE_SPECIAL_REWARDING;

    @Property(key = "gameserver.pvp.special_reward.chance", defaultValue = "2")
    public static float SPECIAL_REWARD_CHANCE;
    // Pvp Instance Enable or not
    @Property(key = "gameserver.pvp.darkpoeta", defaultValue = "false")
    public static boolean PVP_DARKPOETA_ENABLE;

    @Property(key = "gameserver.godstone.rate", defaultValue = "1000")
    public static int GODSTONE_CHANCE;

    @Property(key = "gameserver.pvp.soloboost.mapids", defaultValue = "")
    public static int[] SOLOBOOST_MAP_IDS;

    @Property(key = "gameserver.pvp.soloboost.ap", defaultValue = "1")
    public static int SOLOBOOST_AP_PERC;

    @Property(key = "gameserver.pvp.soloboost.xp", defaultValue = "1")
    public static int SOLOBOOST_XP_PERC;

    @Property(key = "gameserver.pvp.soloboost.dp", defaultValue = "1")
    public static int SOLOBOOST_DP_PERC;
}
