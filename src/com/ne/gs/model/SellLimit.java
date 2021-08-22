/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

import java.util.NoSuchElementException;

public enum SellLimit {
    LIMIT_1_30(1, 30, 5300047L),
    LIMIT_31_40(31, 40, 7100047L),
    LIMIT_41_55(41, 55, 12050047L),
    LIMIT_56_60(56, 60, 146000047L);

    private final int playerMinLevel;
    private final int playerMaxLevel;
    private final long limit;

    private SellLimit(int playerMinLevel, int playerMaxLevel, long limit) {
        this.playerMinLevel = playerMinLevel;
        this.playerMaxLevel = playerMaxLevel;
        this.limit = limit;
    }

    public static long getSellLimit(int playerLevel) {
        for (SellLimit sellLimit : values()) {
            if (sellLimit.playerMinLevel <= playerLevel && sellLimit.playerMaxLevel >= playerLevel) {
                return sellLimit.limit;
            }
        }
        throw new NoSuchElementException("Sell limit for player level: " + playerLevel + " was not found");
    }
}
