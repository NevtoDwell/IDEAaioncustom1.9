/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

/**
 * @author ATracer
 */
public enum NpcObjectType {
    NORMAL(1),
    SUMMON(2),
    HOMING(16),
    TRAP(32),
    SKILLAREA(64),
    TOTEM(128),
    // TODO not implemented
    GROUPGATE(256),
    SERVANT(1024),
    PET(2048);// TODO not used

    private NpcObjectType(int id) {
        this.id = id;
    }

    private final int id;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
