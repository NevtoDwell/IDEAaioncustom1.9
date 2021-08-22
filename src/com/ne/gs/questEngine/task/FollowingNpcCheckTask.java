/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.task;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.zone.ZoneName;

public class FollowingNpcCheckTask implements Runnable {

    private final QuestEnv env;
    private final DestinationChecker destinationChecker;

    /**
     * @param destinationChecker
     */
    FollowingNpcCheckTask(QuestEnv env, DestinationChecker destinationChecker) {
        this.env = env;
        this.destinationChecker = destinationChecker;
    }

    @Override
    public void run() {
        final Player player = env.getPlayer();
        Npc npc = (Npc) destinationChecker.follower;
        if (player.getLifeStats().isAlreadyDead() || npc.getLifeStats().isAlreadyDead()) {
            onFail(env);
        }
        if (!MathUtil.isIn3dRange(player, npc, 50)) {
            onFail(env);
        }

        if (destinationChecker.check()) {
            onSuccess(env);
        }
    }

    /**
     * Following task succeeded, proceed with quest
     */
    private final void onSuccess(QuestEnv env) {
        stopFollowing(env);
        QuestEngine.getInstance().onNpcReachTarget(env);
    }

    /**
     * Following task failed, abort further progress
     */
    protected void onFail(QuestEnv env) {
        stopFollowing(env);
        QuestEngine.getInstance().onNpcLostTarget(env);
    }

    private final void stopFollowing(QuestEnv env) {
        Player player = env.getPlayer();
        Npc npc = (Npc) destinationChecker.follower;
        player.getController().cancelTask(TaskId.QUEST_FOLLOW);
        npc.getAi2().onCreatureEvent(AIEventType.STOP_FOLLOW_ME, player);
        if (!npc.getAi2().getName().equals("following")) {
            npc.getController().onDelete();
        }
    }
}

abstract class DestinationChecker {

    protected Creature follower;

    abstract boolean check();
}

final class TargetDestinationChecker extends DestinationChecker {

    private final Creature target;

    /**
     * @param follower
     * @param target
     */
    TargetDestinationChecker(Creature follower, Creature target) {
        this.follower = follower;
        this.target = target;
    }

    @Override
    boolean check() {
        return MathUtil.isIn3dRange(target, follower, 10);
    }
}

final class CoordinateDestinationChecker extends DestinationChecker {

    private final float x;
    private final float y;
    private final float z;

    /**
     * @param follower
     * @param x
     * @param y
     * @param z
     */
    CoordinateDestinationChecker(Creature follower, float x, float y, float z) {
        this.follower = follower;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    boolean check() {
        return MathUtil.isNearCoordinates(follower, x, y, z, 10);
    }
}

final class ZoneChecker extends DestinationChecker {

    private final ZoneName zoneName;

    ZoneChecker(Creature follower, ZoneName zoneName) {
        this.follower = follower;
        this.zoneName = zoneName;
    }

    @Override
    boolean check() {
        return follower.isInsideZone(zoneName);
    }
}

final class ZoneChecker2 extends DestinationChecker {

    private final ZoneName zone1, zone2;

    ZoneChecker2(Creature follower, ZoneName zone1, ZoneName zone2) {
        this.follower = follower;
        this.zone1 = zone1;
        this.zone2 = zone2;
    }

    @Override
    boolean check() {
        return follower.isInsideZone(zone1) || follower.isInsideZone(zone2);
    }
}
