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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.NpcObjectType;
import com.ne.gs.model.gameobjects.Servant;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.properties.FirstTargetAttribute;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonServantEffect")
public class SummonServantEffect extends SummonEffect {

    private static final Logger log = LoggerFactory.getLogger(SummonServantEffect.class);

    @XmlAttribute(name = "skill_id", required = true)
    protected int skillId;

    @Override
    public void applyEffect(Effect effect) {
        Creature effector = effect.getEffector();
        float x = effector.getX();
        float y = effector.getY();
        float z = effector.getZ();
        spawnServant(effect, time, NpcObjectType.SERVANT, x, y, z);
    }

    /**
     * @param effect
     * @param time
     */
    protected Servant spawnServant(Effect effect, int time, NpcObjectType npcObjectType, float x, float y, float z) {
        Creature effector = effect.getEffector();
        int heading = effector.getHeading();
        int worldId = effector.getWorldId();
        int instanceId = effector.getInstanceId();

        Creature target = (Creature) effector.getTarget();
        Creature effected = effect.getEffected();

        SkillTemplate template = effect.getSkillTemplate();

        if (template.getProperties().getFirstTarget() != FirstTargetAttribute.ME && target == null) {
            log.warn("Servant trying to attack null target!!");
            return null;
        }

        SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, heading);
        final Servant servant = VisibleObjectSpawner.spawnServant(spawn, instanceId, effector, skillId, effect.getSkillLevel(), npcObjectType);

        Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                servant.getController().onDelete();
            }
        }, time * 1000);
		servant.getController().addTask(TaskId.DESPAWN, task);
		if (servant.getNpcObjectType() != NpcObjectType.TOTEM)
			servant.getAi2().onCreatureEvent(AIEventType.ATTACK, (target != null ? target: effected));
		return servant;
    }
}
