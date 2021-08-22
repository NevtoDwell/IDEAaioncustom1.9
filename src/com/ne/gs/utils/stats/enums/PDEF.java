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
public enum PDEF {
    WARRIOR(100),
    GLADIATOR(100),
    TEMPLAR(100),
    SCOUT(100),
    ASSASSIN(100),
    RANGER(100),
    MAGE(100),
    SORCERER(100),
    SPIRIT_MASTER(100),
    PRIEST(100),
    CLERIC(100),
    CHANTER(
        100);

    private final int value;

    private PDEF(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
