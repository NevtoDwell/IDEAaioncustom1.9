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

import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.services.summons.SummonsService;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Simple
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonEffect")
public class SummonEffect extends EffectTemplate {

    @XmlAttribute(name = "npc_id", required = true)
    protected int npcId;
    @XmlAttribute(name = "time", required = true)
    protected int time; // in seconds

    @Override
    public void applyEffect(Effect effect) {
        Player effected = (Player) effect.getEffected();
        SummonsService.createSummon(effected, npcId, effect.getSkillId(), effect.getSkillLevel(), time);
        if (time > 0 && (effect.getEffected() instanceof Player)) {
            Player effector = (Player) effect.getEffected();
            final Summon summon = effector.getSummon();
            Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if ((summon != null) && (summon.isSpawned())) {
                        SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);
                    }
                }
            }, time * 1000);
            summon.getController().addTask(TaskId.DESPAWN, task);
        }
    }

    @Override
    public void calculate(Effect effect) {
        effect.addSucessEffect(this);
    }
}
