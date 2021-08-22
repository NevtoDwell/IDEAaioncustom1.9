/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

/**
 * @author Sarynth
 */
public enum SiegeType {

    // Standard
    FORTRESS(0),
    ARTIFACT(1),

    // Balauria Commanders?
    BOSSRAID_LIGHT(2),
    BOSSRAID_DARK(3),

    // Unk
    INDUN(4),
    UNDERPASS(5),
    SOURCE(6);

    private final int typeId;

    private SiegeType(int id) {
        typeId = id;
    }

    public int getTypeId() {
        return typeId;
    }
}
