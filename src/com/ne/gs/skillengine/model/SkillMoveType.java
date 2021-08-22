/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;


/**
 * @author MrPoke
 */
public enum SkillMoveType {

    RESIST(0),
    DEFAULT(16),
    KNOCKBACK(28),
    STAGGER(112),
    PULL(54);

    private final int id;

    private SkillMoveType(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
