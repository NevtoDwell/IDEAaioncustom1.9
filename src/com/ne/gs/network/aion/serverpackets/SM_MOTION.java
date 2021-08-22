/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;
import java.util.Map;

import com.ne.gs.model.gameobjects.player.motion.Motion;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author MrPoke
 */
public class SM_MOTION extends AionServerPacket {

    byte action;
    short motionId;
    int remainingTime;

    int playerId;
    Map<Integer, Motion> activeMotions;

    Collection<Motion> motions;

    byte type;

    /**
     * @param motions
     */
    public SM_MOTION(Collection<Motion> motions) {
        action = 1;
        this.motions = motions;
    }

    /**
     * @param motionId
     * @param remainingTime
     */
    public SM_MOTION(short motionId, int remainingTime) {
        action = 2;
        this.motionId = motionId;
        this.remainingTime = remainingTime;
    }

    /**
     * @param motionId
     */
    public SM_MOTION(short motionId, byte type) {
        action = 5;
        this.motionId = motionId;
        this.type = type;
    }

    /**
     * @param motionId
     */
    public SM_MOTION(short motionId) {
        action = 6;
        this.motionId = motionId;
    }

    /**
     * @param playerId
     * @param activeMotions
     */
    public SM_MOTION(int playerId, Map<Integer, Motion> activeMotions) {
        action = 7;
        this.playerId = playerId;
        this.activeMotions = activeMotions;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(action);
        switch (action) {
            case 1:
                writeH(motions.size());
                for (Motion motion : motions) {
                    writeH(motion.getId());
                    writeD(motion.getRemainingTime());
                    writeC(motion.isActive() ? 1 : 0);
                }
                break;
            case 2: // Add motion
                writeH(motionId);
                writeD(remainingTime);
                break;
            case 5: // Set motion
                writeH(motionId);
                writeC(type);
                break;
            case 6: // remove
                writeH(motionId);
                break;
            case 7: // Player motions
                writeD(playerId);
                for (int i = 1; i < 6; i++) {
                    Motion motion = activeMotions.get(i);
                    if (motion == null) {
                        writeH(0);
                    } else {
                        writeH(motion.getId());
                    }
                }
        }
    }
}
