/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.state;

/**
 * @author Sweetkr
 */
public enum CreatureVisualState {
    VISIBLE(0),
    // Normal
    HIDE1(1),
    // Hide I
    HIDE2(2),
    // Hide II
    HIDE3(3),
    // Hide by Artifact?
    HIDE5(5),
    // No idea :D
    HIDE10(10),
    // Hide from Npc?
    HIDE13(13),
    // Hide from Npc?
    HIDE20(20),
    // Hide from Npc?
    BLINKING(64); // Blinking when entering to zone

    private final int id;

    private CreatureVisualState(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
