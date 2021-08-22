/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.summons;

public enum SummonMode {
    ATTACK(0),
    GUARD(1),
    REST(2),
    RELEASE(3),
    UNK(5);

    private final int id;

    private SummonMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SummonMode getSummonModeById(int id) {
        for (SummonMode mode : values()) {
            if (mode.getId() == id) {
                return mode;
            }
        }
        return null;
    }
}
