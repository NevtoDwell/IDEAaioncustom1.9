/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.legacy;

/**
 * @author KKnD
 */
public enum LootDistribution {

    NORMAL(0),
    ROLL_DICE(2),
    BID(3);

    private final int id;

    LootDistribution(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
