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
 * @author Lyahim
 */
public enum GroupEvent {

    LEAVE(0),
    MOVEMENT(1),
    DISCONNECTED(3),
    JOIN(5),
    ENTER_OFFLINE(7),
    ENTER(13),
    UPDATE(13),
    UNK(9); // to do
    private final int id;

    private GroupEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
