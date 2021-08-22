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
 * @author ATracer, Sweetkr
 */
public enum CreatureState {
    ACTIVE(1),
    FLYING(1 << 1),
    FLIGHT_TELEPORT(1 << 1),
    RESTING(1 << 2),
    DEAD(3 << 1),
    CHAIR(3 << 1),
    FLOATING_CORPSE(1 << 3),
    PRIVATE_SHOP(5 << 1),
    LOOTING(3 << 2),
    WEAPON_EQUIPPED(1 << 5),
    WALKING(1 << 6),
    NPC_IDLE(1 << 6),
    POWERSHARD(1 << 7),
    TREATMENT(1 << 8),
    GLIDING(1 << 9),

    /**
     * Player just entered Wind Stream
     */
    ENTERED_WINDS(1 << 10),

    DUELING(1 << 11),

    /**
     * Universal state, e.g. can be used for event/instance registation
     */
    EVENT(1 << 20)
    //
    ;

    private final int id;

    private CreatureState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
