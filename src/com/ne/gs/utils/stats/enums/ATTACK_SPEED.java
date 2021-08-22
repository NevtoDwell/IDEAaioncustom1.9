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
public enum ATTACK_SPEED {
    WARRIOR(1500),
    GLADIATOR(1500),
    TEMPLAR(1500),
    SCOUT(1500),
    ASSASSIN(1500),
    RANGER(1500),
    MAGE(1500),
    SORCERER(1500),
    SPIRIT_MASTER(1500),
    PRIEST(1500),
    CLERIC(
        1500),
    CHANTER(1500);

    private final int value;

    private ATTACK_SPEED(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
