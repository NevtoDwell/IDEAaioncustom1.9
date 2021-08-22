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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.concurrent.Future;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Homing;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonHomingEffect")
public class SummonHomingEffect extends SummonEffect {

    @XmlAttribute(name = "npc_count", required = true)
    protected int npcCount;
    @XmlAttribute(name = "attack_count", required = true)
    protected int attackCount;
    @XmlAttribute
    protected int delay;

    // TODO homing that uses skills (skillId:629)

    @Override
    public void applyEffect(final Effect effect) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            public void run() {
                SummonHomingEffect.this.StartEffect(effect);
            }

        }, delay);
    }

    public void StartEffect(Effect effect) {
        Creature effector = effect.getEffector();
        float x = effector.getX();
        float y = effector.getY();
        float z = effector.getZ();
        int heading = effector.getHeading();
        int worldId = effector.getWorldId();
        int instanceId = effector.getInstanceId();

        for (int i = 0; i < npcCount; i++) {
            SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, heading);
            final Homing homing = VisibleObjectSpawner.spawnHoming(spawn, instanceId, effector, attackCount, effect.getSkillId(), effect.getSkillLevel());

            if (attackCount > 0) {

                ActionObserver observer = new ActionObserver(ObserverType.ATTACK) {

                    @Override
                    public void attack(Creature creature) {
                        homing.setAttackCount(homing.getAttackCount() - 1);
                        if (homing.getAttackCount() <= 0) {
                            homing.getController().onDelete();
                        }
                    }
                };

                homing.getObserveController().addObserver(observer);
                effect.setActionObserver(observer, position);

            }
            // Schedule a despawn just in case
            Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if ((homing != null) && (homing.isSpawned())) {
                        homing.getController().onDelete();
                    }
                }
            }, 15 * 1000);
            homing.getController().addTask(TaskId.DESPAWN, task);
            homing.getAi2().onCreatureEvent(AIEventType.ATTACK, effect.getEffected());
        }
    }

}
