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
 * @author Sarynth
 */
public enum PlayerAllianceEvent {
    LEAVE(0),
    LEAVE_TIMEOUT(0),
    BANNED(0),

    MOVEMENT(1),

    DISCONNECTED(3),

    JOIN(5),
    ENTER_OFFLINE(7),

    // Similar to 0, 1, 3 -- only the initial information block.
    UNK(9),

    RECONNECT(13),
    ENTER(13),
    UPDATE(13),

    MEMBER_GROUP_CHANGE(5),

    // Extra? Unused?
    APPOINT_VICE_CAPTAIN(13),
    DEMOTE_VICE_CAPTAIN(13),
    APPOINT_CAPTAIN(13);

    private final int id;

    private PlayerAllianceEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
