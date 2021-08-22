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
public enum KNOWLEDGE {
    WARRIOR(90),
    GLADIATOR(90),
    TEMPLAR(90),
    SCOUT(90),
    ASSASSIN(90),
    RANGER(120),
    MAGE(115),
    SORCERER(120),
    SPIRIT_MASTER(115),
    PRIEST(100),
    CLERIC(105),
    CHANTER(
        105);

    private final int value;

    private KNOWLEDGE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
