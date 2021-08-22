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
public enum MAIN_HAND_ATTACK {
    WARRIOR(19),
    GLADIATOR(19),
    TEMPLAR(19),
    SCOUT(18),
    ASSASSIN(19),
    RANGER(18),
    MAGE(16),
    SORCERER(16),
    SPIRIT_MASTER(16),
    PRIEST(17),
    CLERIC(19),
    CHANTER(19);

    private final int value;

    private MAIN_HAND_ATTACK(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
