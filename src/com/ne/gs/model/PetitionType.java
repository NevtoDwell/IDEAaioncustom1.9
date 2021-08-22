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
 * @author zdead
 */
public enum PetitionType {
    CHARACTER_STUCK(256),
    CHARACTER_RESTORATION(512),
    BUG(768),
    QUEST(1024),
    UNACCEPTABLE_BEHAVIOR(1280),
    SUGGESTION(1536),
    INQUIRY(65280);

    private final int element;

    private PetitionType(int id) {
        element = id;
    }

    public int getElementId() {
        return element;
    }
}
