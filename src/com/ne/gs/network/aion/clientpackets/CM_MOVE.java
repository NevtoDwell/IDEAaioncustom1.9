/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.controllers.movement.MovementMask;
import com.ne.gs.controllers.movement.PlayerMoveController;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.player.Coordinates;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_MOVE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.antihack.AntiHackService;
import com.ne.gs.taskmanager.tasks.TeamMoveUpdater;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import mw.engines.geo.math.Vector3f;

/**
 * Packet about player movement.
 *
 * @author -Nemesiss-
 */
public class CM_MOVE extends AionClientPacket {

    private byte type;
    private byte heading;
    private float x;
    private float y;
    private float z;
    private float x2;
    private float y2;
    private float z2;
    private float vehicleX;
    private float vehicleY;
    private float vehicleZ;
    private float vectorX;
    private float vectorY;
    private float vectorZ;
    private byte glideFlag;
    private int unk1;
    private int unk2;

    @Override
    protected void readImpl() {
        Player player = getConnection().getActivePlayer();

        if (player == null || !player.isSpawned()) {
            return;
        }

        x = readF();
        y = readF();
        z = readF();

        heading = (byte) readC();
        type = (byte) readC();

        if ((type & MovementMask.STARTMOVE) == MovementMask.STARTMOVE) {
            if ((type & MovementMask.MOUSE) == 0) {
                vectorX = readF();
                vectorY = readF();
                vectorZ = readF();
                x2 = vectorX + x;
                y2 = vectorY + y;
                z2 = vectorZ + z;
            } else {
                x2 = readF();
                y2 = readF();
                z2 = readF();
            }
        }
        if ((type & MovementMask.GLIDE) == MovementMask.GLIDE) {
            glideFlag = (byte) readC();
        }
        if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
            unk1 = readD();
            unk2 = readD();
            vehicleX = readF();
            vehicleY = readF();
            vehicleZ = readF();
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        // packet was not read correctly
        //mw: u absolutely right
        if (player.getLifeStats().isAlreadyDead()) {
            return;
        }

        if (player.getEffectController().isUnderFear()) {
            return;
        }
        PlayerMoveController m = player.getMoveController();
        m.movementMask = type;
        if (x <= 50.0f && y <= 50.0f || x == 0.0f && y == 0.0f) {
            PacketSendUtility.sendPck(player, new SM_PLAYER_MOVE(player.getX(), player.getY(), player.getZ(), (byte) player.getHeading(), 3));
            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.STOP_GLIDE, 1, 0), true);
            player.getFlyController().endFly(true);
            PacketSendUtility.sendWhiteMessage(player, "bag");
            return;
        }
        if (player.isRMLoc()) {
            player.setRMLoc(false);
            Coordinates save = player.getSaveCoordinates();
            if ((save.getX() != x || save.getY() != y)) {
                m.setNewDirection(save.getX(), save.getY(), save.getZ() + 1);
                World.getInstance().updatePosition(player, save.getX(), save.getY(), save.getZ() + 1, heading);
                PacketSendUtility.broadcastPacketAndReceive(player, new SM_MOVE(player));
                MathUtil.TreadPool(player);
                return;
            }
        }

        // Admin Teleportation
        if (player.getAdminTeleportation() && ((type & MovementMask.STARTMOVE) == MovementMask.STARTMOVE)
                && ((type & MovementMask.MOUSE) == MovementMask.MOUSE)) {
            m.setNewDirection(x2, y2, z2);
            World.getInstance().updatePosition(player, x2, y2, z2, heading);
            PacketSendUtility.broadcastPacketAndReceive(player, new SM_MOVE(player));
        }
        float speed = player.getGameStats().getMovementSpeedFloat();
        if ((type & MovementMask.GLIDE) == MovementMask.GLIDE) {
            m.glideFlag = glideFlag;
            player.getFlyController().switchToGliding();
        } else if (!player.isInsideZoneType(ZoneType.FLY) & player.getAfterFlying() > 0) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_NOGLIDE_AREA_STOP);
            player.getFlyController().endFly(true);
        } else {
            player.getFlyController().onStopGliding(false);
        }

        if (type == 0) {
            player.getController().onStopMove();
            player.getFlyController().onStopGliding(false);
        } else if ((type & MovementMask.STARTMOVE) == MovementMask.STARTMOVE) {
            if ((type & MovementMask.MOUSE) == 0) {
                speed = player.getGameStats().getMovementSpeedFloat();
                m.vectorX = vectorX;
                m.vectorY = vectorY;
                m.vectorZ = vectorZ;
            }

            player.getMoveController().setNewDirection(x2, y2, z2, heading);
            player.getController().onStartMove();
        } else {
            player.getController().onMove();
            if ((type & MovementMask.MOUSE) == 0) {

                speed = player.getGameStats().getMovementSpeedFloat();

                Vector3f direction = new Vector3f(m.vectorX, m.vectorY, m.vectorZ);
                float diff = (System.currentTimeMillis() - m.getLastMoveUpdate()) / 1000f;
                direction.multLocal(diff);

                Vector3f pos = new Vector3f(x, y, z);
                pos.addLocal(direction);

                player.getMoveController().setNewDirection(pos.x, pos.y, pos.z, heading);

                //speed = player.getGameStats().getMovementSpeedFloat();
                //player.getMoveController().setNewDirection(x + m.vectorX * speed * 1.5F, y + m.vectorY * speed * 1.5F, z + m.vectorZ * speed * 1.5F, heading);
            }
        }

        if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
            m.unk1 = unk1;
            m.unk2 = unk2;
            m.vehicleX = vehicleX;
            m.vehicleY = vehicleY;
            m.vehicleZ = vehicleZ;
        }

        if (!AntiHackService.canMove(player, x, y, z, speed, type)) {
            return;
        }

        m.setBegin(x, y, z);

        World.getInstance().updatePosition(player, x, y, z, heading);
        m.updateLastMove();

        if (player.isInGroup2() || player.isInAlliance2()) {
            TeamMoveUpdater.getInstance().startTask(player);
        }

        if ((type & MovementMask.STARTMOVE) == MovementMask.STARTMOVE || type == 0) {
            PacketSendUtility.broadcastPacket(player, new SM_MOVE(player));
        }

        if ((type & MovementMask.FALL) == MovementMask.FALL) {
            m.updateFalling(z);
        } else {
            m.stopFalling(z);
        }

        if (type != 0 && player.isProtectionActive()) {
            player.getController().stopProtectionActiveTask();
        }
    }

    @Override
    public String toString() {
        return "CM_MOVE [type=" + type + ", heading=" + heading + ", x=" + x + ", y=" + y + ", z=" + z + ", x2=" + x2 + ", y2=" + y2 + ", z2=" + z2
                + ", vehicleX=" + vehicleX + ", vehicleY=" + vehicleY + ", vehicleZ=" + vehicleZ + ", vectorX=" + vectorX + ", vectorY=" + vectorY
                + ", vectorZ=" + vectorZ + ", glideFlag=" + glideFlag + ", unk1=" + unk1 + ", unk2=" + unk2 + "]";
    }
}
