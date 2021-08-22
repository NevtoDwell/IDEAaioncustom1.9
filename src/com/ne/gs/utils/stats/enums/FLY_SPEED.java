/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.stats.enums;

/**
 * @author ATracer
 */
public enum FLY_SPEED {
    WARRIOR(9),
    GLADIATOR(9),
    TEMPLAR(9),
    SCOUT(9),
    ASSASSIN(9),
    RANGER(9),
    MAGE(9),
    SORCERER(9),
    SPIRIT_MASTER(9),
    PRIEST(9),
    CLERIC(9),
    CHANTER(9);

    private final int value;

    private FLY_SPEED(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
