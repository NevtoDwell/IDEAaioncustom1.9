/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.concurrent.Future;

import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.GroupGate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author LokiReborn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonGroupGateEffect")
public class SummonGroupGateEffect extends SummonEffect {

    @Override
    public void applyEffect(Effect effect) {

        Creature effector = effect.getEffector();
        float x = effector.getX();
        float y = effector.getY();
        float z = effector.getZ();
        int heading = effector.getHeading();
        int worldId = effector.getWorldId();
        int instanceId = effector.getInstanceId();

        SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, heading);
        final GroupGate groupgate = VisibleObjectSpawner.spawnGroupGate(spawn, instanceId, effector);

        Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                groupgate.getController().onDelete();
            }
        }, time * 1000);
        groupgate.getController().addTask(TaskId.DESPAWN, task);
    }
}
