/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.trade;

import com.ne.gs.configs.main.PricesConfig;
import com.ne.gs.configs.main.SiegeConfig;
import com.ne.gs.model.Race;
import com.ne.gs.model.siege.Influence;

/**
 * @author Sarynth modified by wakizashi Used to get prices for the player. - Packets: SM_PRICES, SM_TRADELIST, SM_SELL_ITEM - Services: Godstone socket,
 *         teleporter, other fees. TODO: Add Player owner; value and check for PremiumRates or faction price influence.
 */
public final class PricesService {

    /**
     * Used in SM_PRICES
     *
     * @return buyingPrice
     */
    public static int getGlobalPrices(Race playerRace) {
        int defaultPrices = PricesConfig.DEFAULT_PRICES;

        if (!SiegeConfig.SIEGE_ENABLED) {
            return defaultPrices;
        }

        float influenceValue = 0;
        switch (playerRace) {
            case ASMODIANS:
                influenceValue = Influence.getInstance().getGlobalAsmodiansInfluence();
                break;
            case ELYOS:
                influenceValue = Influence.getInstance().getGlobalElyosInfluence();
                break;
            default:
                influenceValue = 0.5f;
                break;
        }
        if (influenceValue == 0.5f) {
            return defaultPrices;
        } else if (influenceValue > 0.5f) {
            float diff = influenceValue - 0.5f;
            return Math.round(defaultPrices - ((diff / 2) * 100));
        } else {
            float diff = 0.5f - influenceValue;
            return Math.round(defaultPrices + ((diff / 2) * 100));
        }
    }

    /**
     * Used in SM_PRICES
     *
     * @return
     */
    public static int getGlobalPricesModifier() {
        return PricesConfig.DEFAULT_MODIFIER;
    }

    /**
     * Used in SM_PRICES
     *
     * @return taxes
     */
    public static int getTaxes(Race playerRace) {
        int defaultTax = PricesConfig.DEFAULT_TAXES;

        if (!SiegeConfig.SIEGE_ENABLED) {
            return defaultTax;
        }

        float influenceValue = 0;
        switch (playerRace) {
            case ASMODIANS:
                influenceValue = Influence.getInstance().getGlobalAsmodiansInfluence();
                break;
            case ELYOS:
                influenceValue = Influence.getInstance().getGlobalElyosInfluence();
                break;
            default:
                influenceValue = 0.5f;
                break;
        }
        if (influenceValue >= 0.5f) {
            return defaultTax;
        }
        float diff = 0.5f - influenceValue;
        return Math.round(defaultTax + ((diff / 4) * 100));
    }

    /**
     * Used in SM_TRADELIST.
     *
     * @return buyPriceModifier
     */
    public static int getVendorBuyModifier() {
        return PricesConfig.VENDOR_BUY_MODIFIER;
    }

    /**
     * Used in SM_SELL_ITEM - Can be unique per NPC!
     *
     * @return sellingModifier
     */
    public static int getVendorSellModifier(Race playerRace) {
        return (int) ((int) ((int) (PricesConfig.VENDOR_SELL_MODIFIER * getGlobalPrices(playerRace) / 100F) * getGlobalPricesModifier() / 100F)
            * getTaxes(playerRace) / 100F);
    }

    /**
     * @param basePrice
     *
     * @return modifiedPrice
     */
    public static long getPriceForService(long basePrice, Race playerRace) {
        // Tricky. Requires multiplication by Prices, Modifier, Taxes
        // In order, and round down each time to match client calculation.
        return (long) ((long) ((long) (basePrice * getGlobalPrices(playerRace) / 100D) * getGlobalPricesModifier() / 100D) * getTaxes(playerRace) / 100D);
    }

    /**
     * @param requiredKinah
     *
     * @return modified requiredKinah
     */
    public static long getKinahForBuy(long requiredKinah, Race playerRace) {
        // Requires double precision for 2mil+ kinah items
        return (long) ((long) ((long) ((long) (requiredKinah * getVendorBuyModifier() / 100.0D) * getGlobalPrices(playerRace) / 100.0D)
            * getGlobalPricesModifier() / 100.0D)
            * getTaxes(playerRace) / 100.0D);
    }

    /**
     * @param kinahReward
     *
     * @return
     */
    public static long getKinahForSell(long kinahReward, Race playerRace) {
        return (long) (kinahReward * getVendorSellModifier(playerRace) / 100D);
    }

}
