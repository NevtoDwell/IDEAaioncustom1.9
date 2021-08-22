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
public enum MAXHP {
    WARRIOR(1.1688f, 1.1688f, 284),
    GLADIATOR(1.3393f, 48.246f, 342),
    TEMPLAR(1.3288f, 51.878f, 281),
    SCOUT(1.0297f, 40.823f, 219),
    ASSASSIN(1.0488f, 40.38f,
        222),
    RANGER(0.5f, 38.5f, 133),
    MAGE(0.7554f, 29.457f, 132),
    SORCERER(0.6352f, 24.852f, 112),
    SPIRIT_MASTER(1, 20.6f, 157),
    PRIEST(1.0303f,
        40.824f, 201),
    CLERIC(0.9277f, 35.988f, 229),
    CHANTER(0.9277f, 35.988f, 229);// not retail like, needs some fixes.

    private final float a;
    private final float b;
    private final float c;

    private MAXHP(float a, float b, float c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public int getMaxHpFor(int level) {
        return Math.round(a * (level - 1) * (level - 1) + b * (level - 1) + c);
    }
}
