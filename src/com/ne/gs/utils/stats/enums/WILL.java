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
public enum WILL {
    WARRIOR(90),
    GLADIATOR(90),
    TEMPLAR(105),
    SCOUT(90),
    ASSASSIN(90),
    RANGER(110),
    MAGE(115),
    SORCERER(110),
    SPIRIT_MASTER(115),
    PRIEST(110),
    CLERIC(110),
    CHANTER(
        110);

    private final int value;

    private WILL(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
