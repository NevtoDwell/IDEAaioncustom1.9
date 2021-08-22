/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;

/**
 * @author ATracer
 */
public interface MoveController {

    void moveToDestination();

    float beginX();
    float beginY();
    float beginZ();

    float getTargetX2();
    float getTargetY2();
    float getTargetZ2();

    void setNewDirection(float x, float y, float z, byte heading);

    void startMovingToDestination();

    void abortMove();

    byte getMovementMask();

    boolean isInMove();

    void setInMove(boolean value);
    
    boolean isJumping();
}
