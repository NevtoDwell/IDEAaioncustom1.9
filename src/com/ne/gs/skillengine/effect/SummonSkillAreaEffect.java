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
import com.ne.gs.model.gameobjects.NpcObjectType;
import com.ne.gs.model.gameobjects.Servant;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonSkillAreaEffect")
public class SummonSkillAreaEffect extends SummonServantEffect {

    @Override
    public void applyEffect(Effect effect) {
        // should only be set if player has no target to avoid errors
        if (effect.getEffector().getTarget() == null) {
            effect.getEffector().setTarget(effect.getEffector());
        }
        float x = effect.getX();
        float y = effect.getY();
        float z = effect.getZ();
        if (x == 0 && y == 0) {
            Creature effected = effect.getEffected();
            x = effected.getX();
            y = effected.getY();
            z = effected.getZ();
        }
        // fix for summon whirlwind
        // TODO revisit later and find better fix - kecimis
        int useTime = time;
        switch (effect.getSkillId()) {
            case 2291:
            case 2292:
            case 2293:
            case 2294:
                useTime = 7;
                break;
        }

        final Servant servant = spawnServant(effect, useTime, NpcObjectType.SKILLAREA, x, y, z);
        Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                servant.getController().useSkill(skillId);
            }
        }, 0, 3000);
        servant.getController().addTask(TaskId.SKILL_USE, task);
    }
}
