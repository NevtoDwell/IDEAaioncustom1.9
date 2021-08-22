/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.follow;

import java.util.concurrent.Future;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.services.summons.SummonsService;
import com.ne.gs.utils.MathUtil;

public class FollowSummonTaskAI implements Runnable {

    private final Creature target;
    private final Summon summon;
    private final Player master;
    private float targetX;
    private float targetY;
    private float targetZ;
    private final Future<?> task;

    public FollowSummonTaskAI(Creature target, Summon summon) {
        this.target = target;
        this.summon = summon;
        master = summon.getMaster();
        task = summon.getMaster().getController().getTask(TaskId.SUMMON_FOLLOW);
        setLeadingCoordinates();
    }

    private void setLeadingCoordinates() {
        targetX = target.getX();
        targetY = target.getY();
        targetZ = target.getZ();
    }

    @Override
    public void run() {
        if (target == null || summon == null || master == null) {
            if (task != null) {
                task.cancel(true);
            }
            return;
        }
        if (!isInMasterRange()) {
            SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.DISTANCE);
            return;
        }
        if (!isInTargetRange()) {
            if (targetX != target.getX() || targetY != target.getY() || targetZ != target.getZ()) {
                setLeadingCoordinates();
                onOutOfTargetRange();
            }
        } else if (!master.equals(target)) {
            onDestination();
        }
    }

    private boolean isInTargetRange() {
        return MathUtil.isIn3dRange(target, summon, 2);
    }

    private boolean isInMasterRange() {
        return MathUtil.isIn3dRange(master, summon, 50);
    }

    protected void onDestination() {
        summon.getAi2().onCreatureEvent(AIEventType.ATTACK, target);
    }

    private void onOutOfTargetRange() {
        summon.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
    }
}
