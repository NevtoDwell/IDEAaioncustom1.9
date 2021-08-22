/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.rates;

/**
 * @author ATracer
 */
public abstract class Rates {

    public abstract float getGroupXpRate();

    public abstract float getXpRate();

    public abstract float getApNpcRate();

    public abstract float getApPlayerGainRate();

    public abstract float getXpPlayerGainRate();

    public abstract float getApPlayerLossRate();

    public abstract float getGatheringXPRate();

    public abstract int getGatheringCountRate();

    public abstract float getCraftingXPRate();

    public abstract float getDropRate();

    public abstract float getQuestXpRate();

    public abstract float getQuestKinahRate();

    public abstract float getQuestApRate();

    public abstract float getDpNpcRate();

    public abstract float getDpPlayerRate();

    public abstract int getCraftCritRate();

    public abstract int getComboCritRate();

    public abstract float getArenaSoloRewardRate();

    public abstract float getArenaFFARewardRate();

    public abstract int getPetFeedingRate();

    /**
     * @param membership
     *
     * @return Rates
     */
    public static Rates getRatesFor(byte membership) {
        switch (membership) {
            case 0:
                return new RegularRates();
            case 1:
                return new PremiumRates();
            case 2:
                return new VipRates();
            default:
                return new VipRates();
        }
    }
}
