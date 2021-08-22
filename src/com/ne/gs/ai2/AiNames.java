/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

/**
 * @author ATracer
 */
public enum AiNames {

    GENERAL_NPC("general"),
    DUMMY_NPC("dummy"),
    AGGRESSIVE_NPC("aggressive");

    private final String name;

    private AiNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
