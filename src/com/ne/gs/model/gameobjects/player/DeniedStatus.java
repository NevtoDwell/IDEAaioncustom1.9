/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

/**
 * @author Sweetkr
 */
public enum DeniedStatus {
    VIEW_DETAILS(1),
    TRADE(2),
    GROUP(4),
    GUILD(8),
    FRIEND(16),
    DUEL(32);

    private final int id;

    private DeniedStatus(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
