/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;

import com.ne.gs.model.gameobjects.Pet;

/**
 * @author ATracer
 */
public class PetMoveController extends CreatureMoveController<Pet> {

    protected float targetDestX;
    protected float targetDestY;
    protected float targetDestZ;
    protected byte heading;
    protected byte movementMask;

    public PetMoveController() {
        super(null);// not used yet
    }

    @Override
    public void moveToDestination() {
    }

    @Override
    public float getTargetX2() {
        return targetDestX;
    }

    @Override
    public float getTargetY2() {
        return targetDestY;
    }

    @Override
    public float getTargetZ2() {
        return targetDestZ;
    }

    @Override
    public void setNewDirection(float x2, float y2, float z2) {
        setNewDirection(x2, y2, z2, (byte) 0);
    }

    @Override
    public void setNewDirection(float x, float y, float z, byte heading) {
        targetDestX = x;
        targetDestY = y;
        targetDestZ = z;
        this.heading = heading;
    }

    @Override
    public void startMovingToDestination() {
    }

    @Override
    public void abortMove() {
    }

    @Override
    public byte getMovementMask() {
        return movementMask;
    }

    @Override
    public boolean isInMove() {
        return true;
    }

    @Override
    public void setInMove(boolean value) {
    }
}
