/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.ne.gs.model.TaskId;
import com.ne.gs.model.drop.DropItem;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.spawns.TemporarySpawn;
import com.ne.gs.services.drop.DropRegistrationService;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.ai2.event.AIEventType;

/**
 * @author ATracer, Source, xTz
 */
public final class RespawnService {
    private static final long DECAY_NOW_MS = TimeUnit.SECONDS.toMillis(5);
    private static final long DECAY_WITHOUT_DROP_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long DECAY_WITH_DROP_MS = TimeUnit.MINUTES.toMillis(5);
    private static final long DECAY_QUEST_WITH_DROP_MS = TimeUnit.SECONDS.toMillis(30);

    public static Future<?> decayNow(Npc npc) {
        return decayNow(npc.getObjectId());
    }

    public static Future<?> decayNow(Integer npcUid) {
        return scheduleDecayTask(npcUid, DECAY_NOW_MS);
    }

    public static Future<?> scheduleDecayTask(Npc npc) {
        long decayInterval;
        boolean isQuestuseitem = npc.getAi2().getName().equals("quest_use_item");
        Set<DropItem> drop = DropRegistrationService.getInstance().geCurrentDropMap().get(npc.getObjectId());

        if (drop == null) {
            decayInterval = DECAY_NOW_MS;
        } else if (drop.isEmpty()) {
            decayInterval = DECAY_WITHOUT_DROP_MS;
        } else if (isQuestuseitem) {
            decayInterval = DECAY_QUEST_WITH_DROP_MS;
        }
        else {
            decayInterval = DECAY_WITH_DROP_MS;
        }

        return scheduleDecayTask(npc, decayInterval);
    }

    public static Future<?> scheduleDecayTask(Npc npc, long decayInterval) {
        return scheduleDecayTask(npc.getObjectId(), decayInterval);
    }

    public static Future<?> scheduleDecayTask(Integer npcUid, long decayInterval) {
        return ThreadPoolManager.getInstance().schedule(new DecayTask(npcUid), decayInterval);
    }

    public static Future<?> scheduleRespawnTask(VisibleObject visibleObject) {
        int interval = visibleObject.getSpawn().getRespawnTime();
        SpawnTemplate spawnTemplate = visibleObject.getSpawn();
        int instanceId = visibleObject.getInstanceId();
        return ThreadPoolManager.getInstance().schedule(new RespawnTask(spawnTemplate, instanceId), interval * 1000);
    }

    private static VisibleObject respawn(SpawnTemplate spawnTemplate, int instanceId) {
        if (spawnTemplate.isTemporarySpawn()) {
            TemporarySpawn ts = spawnTemplate.getTemporarySpawn();
            if (!ts.canSpawn() && !ts.isInSpawnTime()) {
                return null;
            }
        }

        int worldId = spawnTemplate.getWorldId();
        boolean instanceExists = InstanceService.isInstanceExist(worldId, instanceId);
        if (spawnTemplate.isNoRespawn() || !instanceExists) {
            return null;
        }

        if (spawnTemplate.hasPool()) {
            spawnTemplate = spawnTemplate.changeTemplate();
        }
        return SpawnEngine.spawnObject(spawnTemplate, instanceId);
    }

    private static class RespawnTask implements Runnable {
        private final SpawnTemplate spawn;
        private final int instanceId;

        RespawnTask(SpawnTemplate spawn, int instanceId) {
            this.spawn = spawn;
            this.instanceId = instanceId;
        }

        @Override
        public void run() {
            VisibleObject visibleObject = spawn.getVisibleObject();
            if (visibleObject != null && (visibleObject instanceof Npc)) {
                ((Npc) visibleObject).getController().cancelTask(TaskId.RESPAWN);
            }
            RespawnService.respawn(spawn, instanceId);
        }
    }

    private static class DecayTask implements Runnable {
        private final int npcId;

        DecayTask(int npcId) {
            this.npcId = npcId;
        }

        @Override
        public void run() {
            VisibleObject visibleObject = World.getInstance().findVisibleObject(npcId);
            if (visibleObject != null) {
                visibleObject.getController().onDelete();
            }
        }
    }
}
