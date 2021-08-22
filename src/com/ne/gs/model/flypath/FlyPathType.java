/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.flypath;

public enum FlyPathType {
    GEYSER(0),
    ONE_WAY(1),
    TWO_WAY(2);

    private final int id;

    private FlyPathType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
