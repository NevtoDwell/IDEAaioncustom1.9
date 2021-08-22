/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;

import com.ne.gs.ai2.AISubState;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.taskmanager.tasks.MoveTaskManager;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;

public class SiegeWeaponMoveController extends SummonMoveController {

    private float pointX;
    private float pointY;
    private float pointZ;
    private final float offset = 0.1F;
    public static final float MOVE_CHECK_OFFSET = 0.1F;

    public SiegeWeaponMoveController(Summon owner) {
        super(owner);
    }

    @Override
    public void moveToDestination() {
        if (!owner.canPerformMove() || owner.getAi2().getSubState() == AISubState.CAST) {
            if (started.compareAndSet(true, false)) {
                setAndSendStopMove(owner);
            }
            updateLastMove();
            return;
        }
        if (started.compareAndSet(false, true)) {
            movementMask = -32;
            PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
        }

        if (MathUtil.getDistance(owner.getTarget(), pointX, pointY, pointZ) > MOVE_CHECK_OFFSET) {
            pointX = owner.getTarget().getX();
            pointY = owner.getTarget().getY();
            pointZ = owner.getTarget().getZ();
        }
        moveToLocation(pointX, pointY, pointZ, offset);
        updateLastMove();
    }

    @Override
    public void moveToTargetObject() {
        updateLastMove();
        MoveTaskManager.getInstance().addCreature(owner);
    }

    protected void moveToLocation(float targetX, float targetY, float targetZ, float offset) {
        float ownerX = owner.getX();
        float ownerY = owner.getY();
        float ownerZ = owner.getZ();

        boolean directionChanged = targetX != targetDestX || targetY != targetDestY || targetZ != targetDestZ;

        if (directionChanged) {
            heading = (byte) (int) (Math.toDegrees(Math.atan2(targetY - ownerY, targetX - ownerX)) / 3);
        }

        targetDestX = targetX;
        targetDestY = targetY;
        targetDestZ = targetZ;

        float currentSpeed = owner.getGameStats().getMovementSpeedFloat();
        float futureDistPassed = currentSpeed * (System.currentTimeMillis() - lastMoveUpdate) / 1000.0F;

        float dist = (float) MathUtil.getDistance(ownerX, ownerY, ownerZ, targetX, targetY, targetZ);

        if (dist == 0) {
            return;
        }

        if (futureDistPassed > dist) {
            futureDistPassed = dist;
        }

        float distFraction = futureDistPassed / dist;
        float newX = (targetDestX - ownerX) * distFraction + ownerX;
        float newY = (targetDestY - ownerY) * distFraction + ownerY;
        float newZ = (targetDestZ - ownerZ) * distFraction + ownerZ;
        World.getInstance().updatePosition(owner, newX, newY, newZ, heading, false);
        if (directionChanged) {
            movementMask = -32;
            PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
        }
    }
}
