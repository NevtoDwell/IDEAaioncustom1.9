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
public enum HEALTH {
    WARRIOR(110),
    GLADIATOR(115),
    TEMPLAR(100),
    SCOUT(100),
    ASSASSIN(100),
    RANGER(90),
    MAGE(90),
    SORCERER(90),
    SPIRIT_MASTER(90),
    PRIEST(95),
    CLERIC(110),
    CHANTER(
        105);

    private final int value;

    private HEALTH(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
