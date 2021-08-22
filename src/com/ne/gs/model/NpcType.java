/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

/**
 * @author Luno modified by Wakizashi (CHEST)
 */
public enum NpcType {
    /**
     * These are regular monsters
     */
    ATTACKABLE(0),
    /**
     * These are monsters that are pre-aggressive
     */
    PEACE(2),
    AGGRESSIVE(8),
    /**
     * These are non attackable NPCs
     */
    INVULNERABLE(10),
    NON_ATTACKABLE(38);

    private final int someClientSideId;

    private NpcType(int id) {
        someClientSideId = id;
    }

    public int getId() {
        return someClientSideId;
    }
}
