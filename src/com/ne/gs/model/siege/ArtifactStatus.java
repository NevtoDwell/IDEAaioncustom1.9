/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

/**
 * @author MrPoke
 */
public enum ArtifactStatus {

    IDLE(0),
    ACTIVATION(1),
    CASTING(2),
    ACTIVATED(3);

    private final int id;

    ArtifactStatus(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
}
