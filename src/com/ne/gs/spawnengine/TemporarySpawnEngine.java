/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import javolution.util.FastList;

import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.spawns.TemporarySpawn;

public final class TemporarySpawnEngine {

    private static final FastList<SpawnGroup2> temporarySpawns = new FastList<>();

    public static void spawnAll() {
        spwan(true);
    }

    public static void onHourChange() {
        despawn();
        spwan(false);
    }

    private static void despawn() {
        for (SpawnGroup2 spawn : temporarySpawns) {
            for (SpawnTemplate template : spawn.getSpawnTemplates()) {
                if (template.getTemporarySpawn().canDespawn()) {
                    VisibleObject object = template.getVisibleObject();
                    if (object != null) {
                        if ((object instanceof Npc)) {
                            Npc npc = (Npc) object;
                            if ((!npc.getLifeStats().isAlreadyDead()) && (template.hasPool())) {
                                template.setUse(false);
                            }
                            npc.getController().cancelTask(TaskId.RESPAWN);
                        }
                        if (object.isSpawned()) {
                            object.getController().onDelete();
                        }
                    }
                }
            }
        }
    }

    private static void spwan(boolean startCheck) {
        for (SpawnGroup2 spawn : temporarySpawns) {
            if (spawn.hasPool()) {
                TemporarySpawn temporarySpawn = spawn.geTemporarySpawn();
                if ((temporarySpawn.canSpawn()) || ((startCheck) && (spawn.getRespawnTime() != 0) && (temporarySpawn.isInSpawnTime()))) {
                    for (int pool = 0; pool < spawn.getPool(); pool++) {
                        SpawnTemplate template = spawn.getRndTemplate();
                        SpawnEngine.spawnObject(template, 1);
                    }
                }
            } else {
                for (SpawnTemplate template : spawn.getSpawnTemplates()) {
                    TemporarySpawn temporarySpawn = template.getTemporarySpawn();
                    if ((temporarySpawn.canSpawn()) || ((startCheck) && (!template.isNoRespawn()) && (temporarySpawn.isInSpawnTime()))) {
                        SpawnEngine.spawnObject(template, 1);
                    }
                }
            }
        }
    }

    public static void addSpawnGroup(SpawnGroup2 spawn) {
        temporarySpawns.add(spawn);
    }
}
