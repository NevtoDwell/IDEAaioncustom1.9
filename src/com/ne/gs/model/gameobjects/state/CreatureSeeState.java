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
public enum CreatureSeeState {
    NORMAL(0),
    // Normal
    SEARCH1(1),
    // See-Through: Hide I
    SEARCH2(2),
    // See-Through: Hide II
    SEARCH5(5),
    // no idea :)
    SEARCH10(10);

    private final int id;

    private CreatureSeeState(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
