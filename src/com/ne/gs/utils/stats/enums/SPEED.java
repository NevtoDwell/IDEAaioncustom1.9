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
public enum SPEED {
    WARRIOR(6),
    GLADIATOR(6),
    TEMPLAR(6),
    SCOUT(6),
    ASSASSIN(6),
    RANGER(6),
    MAGE(6),
    SORCERER(6),
    SPIRIT_MASTER(6),
    PRIEST(6),
    CLERIC(6),
    CHANTER(6);

    private final int value;

    private SPEED(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
