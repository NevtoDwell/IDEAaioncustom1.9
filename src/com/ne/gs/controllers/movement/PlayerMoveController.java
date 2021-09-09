/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;

import com.ne.gs.configs.main.FallDamageConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.stats.StatFunctions;

/**
 * @author ATracer
 */
public class PlayerMoveController extends PlayableMoveController<Player> {

    private float fallDistance;
    private float lastFallZ;
     private long lastJumpUpdate;

    public PlayerMoveController(Player owner) {
        super(owner);
        beginX = owner.getX();
        beginY = owner.getY();
        beginZ = owner.getZ();
    }

    public final void updateLastJump() {
        lastJumpUpdate = System.currentTimeMillis();
    }
    
    @Override
    public boolean isJumping() {
        return System.currentTimeMillis() - lastJumpUpdate < 1000;
    }
    
    private float beginX, beginY, beginZ;

    @Override
    public float beginX(){
        return beginX;
    }

    @Override
    public float beginY(){
        return beginY;
    }

    @Override
    public float beginZ(){
        return beginZ;
    }

    public void setBegin(float x, float y, float z){
        beginX = x;
        beginY = y;
        beginZ = z;
    }

    @Override
    public void moveToDestination()
    {
        super.moveToDestination();
    }

    public void updateFalling(float newZ) {
        if (lastFallZ != 0) {
            fallDistance += lastFallZ - newZ;
            if (fallDistance >= FallDamageConfig.MAXIMUM_DISTANCE_MIDAIR) {
                StatFunctions.calculateFallDamage(owner, fallDistance, false);
            }
        }
        lastFallZ = newZ;
        owner.getObserveController().notifyMoveObservers();
    }

    public void stopFalling(float newZ) { 
        if (lastFallZ != 0) { 
            if (!owner.isFlying() && fallDistance < FallDamageConfig.MAXIMUM_DISTANCE_MIDAIR ) { //fix двойной смерти.
                StatFunctions.calculateFallDamage(owner, fallDistance, true);
            }
            fallDistance = 0;
            lastFallZ = 0;
            owner.getObserveController().notifyMoveObservers();
        }
    }

}
