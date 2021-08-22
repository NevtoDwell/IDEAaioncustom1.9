/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player.motion;

import java.util.Collections;
import java.util.Map;

import com.ne.gs.database.GDB;
import javolution.util.FastMap;

import com.ne.gs.database.dao.MotionDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_MOTION;
import com.ne.gs.taskmanager.tasks.ExpireTimerTask;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class MotionList {

    private final Player owner;
    private Map<Integer, Motion> activeMotions;
    private Map<Integer, Motion> motions;

    /**
     * @param owner
     */
    public MotionList(Player owner) {
        this.owner = owner;
    }

    /**
     * @return the activeMotions
     */
    public Map<Integer, Motion> getActiveMotions() {
        if (activeMotions == null) {
            return Collections.emptyMap();
        }
        return activeMotions;
    }

    /**
     * @return the motions
     */
    public Map<Integer, Motion> getMotions() {
        if (motions == null) {
            return Collections.emptyMap();
        }
        return motions;
    }

    public void add(Motion motion, boolean persist) {
        if (motions == null) {
            motions = new FastMap<>();
        }
        if (motions.containsKey(motion.getId()) && motion.getExpireTime() == 0) {
            remove(motion.getId());
        }
        motions.put(motion.getId(), motion);
        if (motion.isActive()) {
            if (activeMotions == null) {
                activeMotions = new FastMap<>();
            }
            Motion old = activeMotions.put(Motion.motionType.get(motion.getId()), motion);
            if (old != null) {
                old.setActive(false);
                GDB.get(MotionDAO.class).updateMotion(owner.getObjectId(), old);
            }
        }
        if (persist) {
            if (motion.getExpireTime() != 0) {
                ExpireTimerTask.getInstance().addTask(motion, owner);
            }
            GDB.get(MotionDAO.class).storeMotion(owner.getObjectId(), motion);
        }
    }

    public boolean remove(int motionId) {
        Motion motion = motions.remove(motionId);
        if (motion != null) {
            owner.sendPck(new SM_MOTION((short) motionId));
            GDB.get(MotionDAO.class).deleteMotion(owner.getObjectId(), motionId);
            if (motion.isActive()) {
                activeMotions.remove(Motion.motionType.get(motionId));
                return true;
            }
        }
        return false;
    }

    public void setActive(int motionId, int motionType) {
        if (motionId != 0) {
            Motion motion = motions.get(motionId);
            if (motion == null || motion.isActive()) {
                return;
            }
            if (activeMotions == null) {
                activeMotions = new FastMap<>();
            }
            Motion old = activeMotions.put(motionType, motion);
            if (old != null) {
                old.setActive(false);
                GDB.get(MotionDAO.class).updateMotion(owner.getObjectId(), old);
            }
            motion.setActive(true);
            GDB.get(MotionDAO.class).updateMotion(owner.getObjectId(), motion);
        } else if (activeMotions != null) {
            Motion old = activeMotions.remove(motionType);
            if (old == null) {
                return; // TODO packet hack??
            }
            old.setActive(false);
            GDB.get(MotionDAO.class).updateMotion(owner.getObjectId(), old);
        }
        owner.sendPck(new SM_MOTION((short) motionId, (byte) motionType));
        PacketSendUtility.broadcastPacket(owner, new SM_MOTION(owner.getObjectId(), activeMotions), true);
    }
}
