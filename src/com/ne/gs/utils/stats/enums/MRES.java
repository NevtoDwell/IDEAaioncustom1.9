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
public enum MRES {
    WARRIOR(0),
    GLADIATOR(0),
    TEMPLAR(0),
    SCOUT(0),
    ASSASSIN(0),
    RANGER(0),
    MAGE(0),
    SORCERER(0),
    SPIRIT_MASTER(0),
    PRIEST(0),
    CLERIC(0),
    CHANTER(0);

    private final int value;

    private MRES(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
