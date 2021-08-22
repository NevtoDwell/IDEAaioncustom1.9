/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.taskmanager.tasks.PlayerMoveTaskManager;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.stats.StatFunctions;
import com.ne.gs.world.World;

/**
 * @author ATracer base class for summon & player move controller
 */
public abstract class PlayableMoveController<T extends Creature> extends CreatureMoveController<T> {

    private boolean sendMovePacket = true;
    private int movementHeading = -1;

    public float vehicleX;
    public float vehicleY;
    public float vehicleZ;
    public int vehicleSpeed;

    public float vectorX;
    public float vectorY;
    public float vectorZ;
    public byte glideFlag;
    public int unk1;
    public int unk2;

    public PlayableMoveController(T owner) {
        super(owner);
    }

    @Override
    public void startMovingToDestination() {
        updateLastMove();
        if (owner.canPerformMove()) {
            if (isControlled() && started.compareAndSet(false, true)) {
                movementMask = -32;
                sendForcedMovePacket();
                PlayerMoveTaskManager.addPlayer(owner);
            }
        }
    }

    private final boolean isControlled() {
        return owner.getEffectController().isUnderFear();
    }

    private void sendForcedMovePacket() {
        PacketSendUtility.broadcastPacketAndReceive(owner, new SM_MOVE(owner));
        sendMovePacket = false;
    }

    @Override
    public void moveToDestination() {
        if (!owner.canPerformMove()) {
            if (started.compareAndSet(true, false)) {
                setAndSendStopMove(owner);
            }
            updateLastMove();
            return;
        }

        if (sendMovePacket && isControlled()) {
            sendForcedMovePacket();
        }


        float currentSpeed = StatFunctions.getMovementModifier(owner, StatEnum.SPEED, owner.getGameStats().getMovementSpeedFloat());
        float futureDistPassed = currentSpeed * (System.currentTimeMillis() - lastMoveUpdate) / 1000f;
        float dist = (float) MathUtil.getDistance(owner.getX(), owner.getY(), owner.getZ(), targetDestX, targetDestY, targetDestZ);

        if (dist == 0) {
            return;
        }

        if (futureDistPassed > dist) {
            futureDistPassed = dist;
        }

        float distFraction = futureDistPassed / dist;
        float newX = (targetDestX - owner.getX()) * distFraction + owner.getX();
        float newY = (targetDestY - owner.getY()) * distFraction + owner.getY();
        float newZ = (targetDestZ - owner.getZ()) * distFraction + owner.getZ();

		/*
         * if ((movementMask & MovementMask.MOUSE) == 0) { targetDestX = newX + vectorX; targetDestY = newY + vectorY; targetDestZ = newZ + vectorZ; }
		 */

        World.getInstance().updatePosition(owner, newX, newY, newZ, heading, false);
        updateLastMove();
    }

    @Override
    public void abortMove() {
        started.set(false);
        PlayerMoveTaskManager.removePlayer(owner);
        targetDestX = 0;
        targetDestY = 0;
        targetDestZ = 0;
        setAndSendStopMove(owner);
    }

    @Override
    public void setNewDirection(float x, float y, float z) {
        if (targetDestX != x || targetDestY != y || targetDestZ != z) {
            sendMovePacket = true;
        }
        targetDestX = x;
        targetDestY = y;
        targetDestZ = z;

        float h = MathUtil.calculateAngleFrom(owner.getX(), owner.getY(), targetDestX, targetDestY);
        if (h != 0) {
            int value = (int) (((heading * 3) - h) / 45);
            if (value < 0) {
                value += 8;
            }
            if (movementHeading != value) {
                movementHeading = value;
            }
        }
    }

    public int getMovementHeading() {
        if (!isInMove()) {
            return -1;
        }
        return movementHeading;
    }

}
