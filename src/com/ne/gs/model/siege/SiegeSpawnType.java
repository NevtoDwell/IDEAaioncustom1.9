/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

public enum SiegeSpawnType {

    PEACE(0),
    GUARD(1),
    ARTIFACT(2),
    PROTECTOR(3),
    MINE(4),
    PORTAL(5),
    GENERATOR(6),
    SPRING(7),
    RACEPROTECTOR(8);

    private final int _id;

    private SiegeSpawnType(int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }
}
