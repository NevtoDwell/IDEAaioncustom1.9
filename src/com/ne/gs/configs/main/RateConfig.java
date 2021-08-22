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
public final class RateConfig {

    /**
     * XP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.xp", defaultValue = "1.0")
    public static float XP_RATE;

    @Property(key = "gameserver.rate.premium.xp", defaultValue = "2.0")
    public static float PREMIUM_XP_RATE;

    @Property(key = "gameserver.rate.vip.xp", defaultValue = "3.0")
    public static float VIP_XP_RATE;

    /**
     * Group XP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.group.xp", defaultValue = "1.0")
    public static float GROUPXP_RATE;

    @Property(key = "gameserver.rate.premium.group.xp", defaultValue = "2.0")
    public static float PREMIUM_GROUPXP_RATE;

    @Property(key = "gameserver.rate.vip.group.xp", defaultValue = "3.0")
    public static float VIP_GROUPXP_RATE;

    /**
     * Quest XP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.quest.xp", defaultValue = "2")
    public static float QUEST_XP_RATE;

    @Property(key = "gameserver.rate.premium.quest.xp", defaultValue = "4")
    public static float PREMIUM_QUEST_XP_RATE;

    @Property(key = "gameserver.rate.vip.quest.xp", defaultValue = "6")
    public static float VIP_QUEST_XP_RATE;

    /**
     * Gathering XP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.gathering.xp", defaultValue = "1.0")
    public static float GATHERING_XP_RATE;

    @Property(key = "gameserver.rate.premium.gathering.xp", defaultValue = "2.0")
    public static float PREMIUM_GATHERING_XP_RATE;

    @Property(key = "gameserver.rate.vip.gathering.xp", defaultValue = "3.0")
    public static float VIP_GATHERING_XP_RATE;

    @Property(key = "gameserver.rate.regular.gathering.count", defaultValue = "1")
    public static int GATHERING_COUNT_RATE;

    @Property(key = "gameserver.rate.premium.gathering.count", defaultValue = "1")
    public static int PREMIUM_GATHERING_COUNT_RATE;

    @Property(key = "gameserver.rate.vip.gathering.count", defaultValue = "1")
    public static int VIP_GATHERING_COUNT_RATE;
    /**
     * Crafting XP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.crafting.xp", defaultValue = "1.0")
    public static float CRAFTING_XP_RATE;

    @Property(key = "gameserver.rate.premium.crafting.xp", defaultValue = "2.0")
    public static float PREMIUM_CRAFTING_XP_RATE;

    @Property(key = "gameserver.rate.vip.crafting.xp", defaultValue = "3.0")
    public static float VIP_CRAFTING_XP_RATE;

    /**
     * Quest Kinah Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.quest.kinah", defaultValue = "1.0")
    public static float QUEST_KINAH_RATE;

    @Property(key = "gameserver.rate.premium.quest.kinah", defaultValue = "2.0")
    public static float PREMIUM_QUEST_KINAH_RATE;

    @Property(key = "gameserver.rate.vip.quest.kinah", defaultValue = "3.0")
    public static float VIP_QUEST_KINAH_RATE;

    /**
     * Quest AP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.quest.ap", defaultValue = "1.0")
    public static float QUEST_AP_RATE;

    @Property(key = "gameserver.rate.premium.quest.ap", defaultValue = "2.0")
    public static float PREMIUM_QUEST_AP_RATE;

    @Property(key = "gameserver.rate.vip.quest.ap", defaultValue = "3.0")
    public static float VIP_QUEST_AP_RATE;

    /**
     * Drop Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.drop", defaultValue = "1.0")
    public static float DROP_RATE;

    @Property(key = "gameserver.rate.premium.drop", defaultValue = "2.0")
    public static float PREMIUM_DROP_RATE;

    @Property(key = "gameserver.rate.vip.drop", defaultValue = "3.0")
    public static float VIP_DROP_RATE;

    /**
     * Player Abyss Points Rates (Gain) - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.ap.player.gain", defaultValue = "1.0")
    public static float AP_PLAYER_GAIN_RATE;

    @Property(key = "gameserver.rate.premium.ap.player.gain", defaultValue = "2.0")
    public static float PREMIUM_AP_PLAYER_GAIN_RATE;

    @Property(key = "gameserver.rate.vip.ap.player.gain", defaultValue = "3.0")
    public static float VIP_AP_PLAYER_GAIN_RATE;

    /**
     * Player Experience Points Rates (Gain) - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.xp.player.gain", defaultValue = "1.0")
    public static float XP_PLAYER_GAIN_RATE;

    @Property(key = "gameserver.rate.premium.xp.player.gain", defaultValue = "2.0")
    public static float PREMIUM_XP_PLAYER_GAIN_RATE;

    @Property(key = "gameserver.rate.vip.xp.player.gain", defaultValue = "3.0")
    public static float VIP_XP_PLAYER_GAIN_RATE;

    /**
     * Player Abyss Points Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.ap.player.loss", defaultValue = "1.0")
    public static float AP_PLAYER_LOSS_RATE;

    @Property(key = "gameserver.rate.premium.ap.player.loss", defaultValue = "2.0")
    public static float PREMIUM_AP_PLAYER_LOSS_RATE;

    @Property(key = "gameserver.rate.vip.ap.player.loss", defaultValue = "3.0")
    public static float VIP_AP_PLAYER_LOSS_RATE;

    /**
     * NPC Abyss Points Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.ap.npc", defaultValue = "1.0")
    public static float AP_NPC_RATE;

    @Property(key = "gameserver.rate.premium.ap.npc", defaultValue = "2.0")
    public static float PREMIUM_AP_NPC_RATE;

    @Property(key = "gameserver.rate.vip.ap.npc", defaultValue = "3.0")
    public static float VIP_AP_NPC_RATE;

    /**
     * PVE DP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.dp.npc", defaultValue = "1.0")
    public static float DP_NPC_RATE;

    @Property(key = "gameserver.rate.premium.dp.npc", defaultValue = "2.0")
    public static float PREMIUM_DP_NPC_RATE;

    @Property(key = "gameserver.rate.vip.dp.npc", defaultValue = "3.0")
    public static float VIP_DP_NPC_RATE;

    /**
     * PVP DP Rates - Regular (1), Premium (2), VIP (3)
     */
    @Property(key = "gameserver.rate.regular.dp.player", defaultValue = "1.0")
    public static float DP_PLAYER_RATE;

    @Property(key = "gameserver.rate.premium.dp.player", defaultValue = "2.0")
    public static float PREMIUM_DP_PLAYER_RATE;

    @Property(key = "gameserver.rate.vip.dp.player", defaultValue = "3.0")
    public static float VIP_DP_PLAYER_RATE;

    /**
     * Arenas configure
     */
    @Property(key = "gameserver.rate.regular.pvparena.solo", defaultValue = "1.5")
    public static float PVP_ARENA_SOLO_REWARD_RATE;

    @Property(key = "gameserver.rate.premium.pvparena.solo", defaultValue = "1.5")
    public static float PREMIUM_PVP_ARENA_SOLO_REWARD_RATE;

    @Property(key = "gameserver.rate.vip.pvparena.solo", defaultValue = "1.5")
    public static float VIP_PVP_ARENA_SOLO_REWARD_RATE;

    @Property(key = "gameserver.rate.regular.pvparena.ffa", defaultValue = "1.2")
    public static float PVP_ARENA_FFA_REWARD_RATE;

    @Property(key = "gameserver.rate.premium.pvparena.ffa", defaultValue = "1.2")
    public static float PREMIUM_PVP_ARENA_FFA_REWARD_RATE;

    @Property(key = "gameserver.rate.vip.pvparena.ffa", defaultValue = "1.2")
    public static float VIP_PVP_ARENA_FFA_REWARD_RATE;

    /**
     * Rate which affects amount of required ap for Abyss rank
     */
    @Property(key = "gameserver.rate.ap.rank", defaultValue = "1")
    public static int ABYSS_RANK_RATE;

    @Property(key = "gameserver.rate.regular.pet.feeding", defaultValue = "1")
    public static int PET_FEEDING_RATE_REG;
    @Property(key = "gameserver.rate.premium.pet.feeding", defaultValue = "2")
    public static int PET_FEEDING_RATE_PREM;
    @Property(key = "gameserver.rate.vip.pet.feeding", defaultValue = "3")
    public static int PET_FEEDING_RATE_VIP;


}
