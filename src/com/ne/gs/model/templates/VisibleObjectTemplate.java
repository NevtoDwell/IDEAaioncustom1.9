/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates;

/**
 * @author ATracer
 */
public abstract class VisibleObjectTemplate {

    /**
     * For Npcs it will return npcid from templates xml
     *
     * @return id of object template
     */
    public abstract int getTemplateId();

    /**
     * For Npcs it will return name from templates xml
     *
     * @return name of object
     */
    public abstract String getName();

    /**
     * Name id of object template
     *
     * @return int
     */
    public abstract int getNameId();

    // /**
    // * Global race of the object
    // *
    // * @return
    // */
    // public abstract Race getRace();

    /**
     * @return
     */
    public BoundRadius getBoundRadius() {
        return BoundRadius.DEFAULT;
    }

    /**
     * @return default state
     */
    public int getState() {
        return 0;
    }
}
