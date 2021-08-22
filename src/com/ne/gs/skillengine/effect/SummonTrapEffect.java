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
import java.util.List;
import java.util.Iterator;

import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Trap;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonTrapEffect")
public class SummonTrapEffect extends SummonEffect {

    @XmlAttribute(name = "skill_id", required = true)
    protected int skillId;

    @Override
    public void applyEffect(Effect effect) {
        Creature effector = effect.getEffector();
        if (effect.getEffector().getTarget() == null) {
            effect.getEffector().setTarget(effect.getEffector());
        }
        float x = effect.getX();
        float y = effect.getY();
        float z = effect.getZ();
        if ((x == 0.0F) && (y == 0.0F)) {
            Creature effected = effect.getEffected();
            x = effected.getX();
            y = effected.getY();
            z = effected.getZ();
        }
        int heading = effector.getHeading();
        int worldId = effector.getWorldId();
        int instanceId = effector.getInstanceId();
		
		double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
        float x1 = (float) (Math.cos(radian) * 1.5);
        float y1 = (float) (Math.sin(radian) * 1.5);

        checkMaxTraps(effector);

        SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x + x1, y + y1, z, heading);
        final Trap trap = VisibleObjectSpawner.spawnTrap(spawn, instanceId, effector, skillId);

        Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                trap.getController().onDelete();
            }
        }, time * 1000);
        trap.getController().addTask(TaskId.DESPAWN, task);
    }

    private void checkMaxTraps(Creature effector) {
        List<Trap> traps = effector.getPosition().getWorldMapInstance().getTraps(effector);
        if(traps.size() >= 2) {
            Iterator<Trap> trapIter = traps.iterator();
            Trap t = trapIter.next();
            t.getController().cancelTask(TaskId.DESPAWN);
            t.getController().onDelete();
        }
    }
}
