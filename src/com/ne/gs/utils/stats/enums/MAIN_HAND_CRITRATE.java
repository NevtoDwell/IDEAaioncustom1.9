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
public enum MAIN_HAND_CRITRATE {
    WARRIOR(2),
    GLADIATOR(2),
    TEMPLAR(2),
    SCOUT(3),
    ASSASSIN(3),
    RANGER(3),
    MAGE(1),
    SORCERER(2),
    SPIRIT_MASTER(2),
    PRIEST(2),
    CLERIC(2),
    CHANTER(1);

    private final int value;

    private MAIN_HAND_CRITRATE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
