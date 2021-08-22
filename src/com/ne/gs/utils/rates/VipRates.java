/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.rates;

import com.ne.gs.configs.main.CraftConfig;
import com.ne.gs.configs.main.RateConfig;

/**
 * @author ATracer
 */
public class VipRates extends Rates {

    @Override
    public float getXpRate() {
        return RateConfig.VIP_XP_RATE;
    }

    @Override
    public float getGroupXpRate() {
        return RateConfig.VIP_GROUPXP_RATE;
    }

    @Override
    public float getQuestXpRate() {
        return RateConfig.VIP_QUEST_XP_RATE;
    }

    @Override
    public float getGatheringXPRate() {
        return RateConfig.VIP_GATHERING_XP_RATE;
    }

    @Override
    public int getGatheringCountRate() {
        return RateConfig.VIP_GATHERING_COUNT_RATE;
    }

    @Override
    public float getCraftingXPRate() {
        return RateConfig.VIP_CRAFTING_XP_RATE;
    }

    @Override
    public float getDropRate() {
        return RateConfig.VIP_DROP_RATE;
    }

    @Override
    public float getQuestKinahRate() {
        return RateConfig.VIP_QUEST_KINAH_RATE;
    }

    @Override
    public float getQuestApRate() {
        return RateConfig.VIP_QUEST_AP_RATE;
    }

    @Override
    public float getApPlayerGainRate() {
        return RateConfig.VIP_AP_PLAYER_GAIN_RATE;
    }

    @Override
    public float getXpPlayerGainRate() {
        return RateConfig.VIP_XP_PLAYER_GAIN_RATE;
    }

    @Override
    public float getApPlayerLossRate() {
        return RateConfig.VIP_AP_PLAYER_LOSS_RATE;
    }

    @Override
    public float getApNpcRate() {
        return RateConfig.VIP_AP_NPC_RATE;
    }

    @Override
    public float getDpNpcRate() {
        return RateConfig.VIP_DP_NPC_RATE;
    }

    @Override
    public float getDpPlayerRate() {
        return RateConfig.VIP_DP_PLAYER_RATE;
    }

    @Override
    public int getCraftCritRate() {
        return CraftConfig.VIP_CRAFT_CRIT_RATE;
    }

    @Override
    public int getComboCritRate() {
        return CraftConfig.VIP_CRAFT_COMBO_RATE;
    }

    @Override
    public float getArenaSoloRewardRate() {
        return RateConfig.VIP_PVP_ARENA_SOLO_REWARD_RATE;
    }

    @Override
    public float getArenaFFARewardRate() {
        return RateConfig.VIP_PVP_ARENA_FFA_REWARD_RATE;
    }

    @Override
    public int getPetFeedingRate() {
        return RateConfig.PET_FEEDING_RATE_VIP;
    }
}
