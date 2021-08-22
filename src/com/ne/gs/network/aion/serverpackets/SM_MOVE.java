/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.controllers.movement.MoveController;
import com.ne.gs.controllers.movement.MovementMask;
import com.ne.gs.controllers.movement.PlayableMoveController;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * This packet is displaying movement of players etc.
 *
 * @author -Nemesiss-
 */
public class SM_MOVE extends AionServerPacket {

    /**
     * Object that is moving.
     */
    private final Creature creature;

    public SM_MOVE(Creature creature) {
        this.creature = creature;
    }

    /**
     * Movement event owner uid
     */
    private int _objectId;

    /**
     * Movement begin point coordinates
     */
    private float _sX,_sY,_sZ;

    /**
     * Movement target point coordinates
     */
    private float _tX,_tY,_tZ;

    /**
     * Movement direction
     */
    private byte _heading;

    /**
     * Movement type flag
     */
    private byte _moveTypeFlag;


    /**
     * Movement notice message constructor
     *
     * @param objectId Movement event owner
     * @param sX Movement begin point X coordinate
     * @param sY Movement begin point Y coordinate
     * @param sZ Movement begin point Z coordinate
     * @param tX Movement end point X coordinate
     * @param tY Movement end point Y coordinate
     * @param tZ Movement end point Z coordinate
     * @param heading Movement direction
     * @param flag Movement type flag
     */
    public SM_MOVE(
            int objectId,
            float sX, float sY, float sZ,
            float tX, float tY, float tZ,
            byte heading,
            byte flag){

        creature = null;

        _objectId= objectId;

        _sX = sX;
        _sY = sY;
        _sZ = sZ;

        _tX = tX;
        _tY = tY;
        _tZ = tZ;

        _heading = heading;
        _moveTypeFlag = flag;
    }

    @Override
    protected void writeImpl(AionConnection client) {

        if (creature == null){

            writeD(_objectId);

            writeF(_sX);
            writeF(_sY);
            writeF(_sZ);

            writeC(_heading);
            writeC(_moveTypeFlag);

            if ((_moveTypeFlag & MovementMask.STARTMOVE) == MovementMask.STARTMOVE ||
                    (_moveTypeFlag & MovementMask.NPC_STARTMOVE) == MovementMask.NPC_STARTMOVE) {
                writeF(_tX);
                writeF(_tY);
                writeF(_tZ);
            }

        } else{
            MoveController moveData = creature.getMoveController();
            writeD(creature.getObjectId());
            writeF(creature.getX());
            writeF(creature.getY());
            writeF(creature.getZ());
            writeC(creature.getHeading());

            writeC(moveData.getMovementMask());

            if (moveData instanceof PlayableMoveController) {
                PlayableMoveController<?> playermoveData = (PlayableMoveController<?>) moveData;
                if ((moveData.getMovementMask() & MovementMask.STARTMOVE) == MovementMask.STARTMOVE) {
                    if ((moveData.getMovementMask() & MovementMask.MOUSE) == 0) {
                        writeF(playermoveData.vectorX);
                        writeF(playermoveData.vectorY);
                        writeF(playermoveData.vectorZ);
                    } else {
                        writeF(moveData.getTargetX2());
                        writeF(moveData.getTargetY2());
                        writeF(moveData.getTargetZ2());
                    }
                }
                if ((moveData.getMovementMask() & MovementMask.GLIDE) == MovementMask.GLIDE) {
                    writeC(playermoveData.glideFlag);
                }
                if ((moveData.getMovementMask() & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
                    writeD(playermoveData.unk1);
                    writeD(playermoveData.unk2);
                    writeF(playermoveData.vectorX);
                    writeF(playermoveData.vectorY);
                    writeF(playermoveData.vectorZ);
                }
            } else if ((moveData.getMovementMask() & MovementMask.STARTMOVE) == MovementMask.STARTMOVE) {
                writeF(moveData.getTargetX2());
                writeF(moveData.getTargetY2());
                writeF(moveData.getTargetZ2());
            }
        }
    }
}
