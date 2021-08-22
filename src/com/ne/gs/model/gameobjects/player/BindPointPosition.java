/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import com.ne.gs.model.gameobjects.PersistentState;

/**
 * @author evilset
 */
public class BindPointPosition {

    private final int mapId;
    private final float x;
    private final float y;
    private final float z;
    private final int heading;
    private PersistentState persistentState;

    /**
     * @param mapId
     * @param x
     * @param y
     * @param z
     * @param heading
     */
    public BindPointPosition(int mapId, float x, float y, float z, int heading) {
        this.mapId = mapId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.heading = heading;
        persistentState = PersistentState.NEW;
    }

    /**
     * @return Returns the mapId.
     */
    public int getMapId() {
        return mapId;
    }

    /**
     * @return Returns the x.
     */
    public float getX() {
        return x;
    }

    /**
     * @return Returns the y.
     */
    public float getY() {
        return y;
    }

    /**
     * @return Returns the z.
     */
    public float getZ() {
        return z;
    }

    /**
     * @return Returns the heading.
     */
    public int getHeading() {
        return heading;
    }

    /**
     * @return the persistentState
     */
    public PersistentState getPersistentState() {
        return persistentState;
    }

    /**
     * @param persistentState
     *     the persistentState to set
     */
    public void setPersistentState(PersistentState persistentState) {
        switch (persistentState) {
            case UPDATE_REQUIRED:
                if (this.persistentState == PersistentState.NEW) {
                    break;
                }
            default:
                this.persistentState = persistentState;
        }
    }
}
