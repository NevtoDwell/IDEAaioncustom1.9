/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public abstract class CreatureMoveController<T extends VisibleObject> implements MoveController {

    protected T owner;
    protected byte heading;
    protected long lastMoveUpdate = System.currentTimeMillis();
    protected boolean isInMove = false;
    protected transient AtomicBoolean started = new AtomicBoolean(false);

    // TODO [AT] not good ...
    public volatile byte movementMask;
    protected float targetDestX;
    protected float targetDestY;
    protected float targetDestZ;


    public CreatureMoveController(T owner) {
        this.owner = owner;
    }

    /**
     * Return x position before movement was started or current position if creature not moved
     */
    @Override
    public float beginX(){
        return owner.getX();
    }

    /**
     * Return y position before movement was started or current position if creature not moved
     */
    @Override
    public float beginY(){
        return owner.getY();
    }

    /**
     * Return z position before movement was started or current position if creature not moved
     */
    @Override
    public float beginZ(){
        return owner.getZ();
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
    public void setNewDirection(float x, float y, float z, byte heading) {
        this.heading = heading;
        setNewDirection(x, y, z);
    }

    protected void setNewDirection(float x, float y, float z) {
        this.targetDestX = x;
        this.targetDestY = y;
        this.targetDestZ = z;
    }

    @Override
    public void startMovingToDestination() {
    }

    @Override
    public void abortMove() {
    }

    protected void setAndSendStopMove(Creature owner) {
        movementMask = 0;
        PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
    }

    public final void updateLastMove() {
        lastMoveUpdate = System.currentTimeMillis();
    }

    /**
     * @return the lastMoveUpdate
     */
    public long getLastMoveUpdate() {
        return lastMoveUpdate;
    }
    
    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public byte getMovementMask() {
        return movementMask;
    }

    @Override
    public boolean isInMove() {
        return isInMove;
    }

    @Override
    public void setInMove(boolean value) {
        isInMove = value;
    }

}
