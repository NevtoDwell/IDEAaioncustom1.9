/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.container;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.services.LifeStatsRestoreService;

/**
 * @author ATracer
 */
public class NpcLifeStats extends CreatureLifeStats<Npc> {

    /**
     * @param owner
     */
    public NpcLifeStats(Npc owner) {
        super(owner, owner.getGameStats().getMaxHp().getCurrent(), owner.getGameStats().getMaxMp().getCurrent());
    }

    @Override
    protected void onIncreaseHp(TYPE type, int value, int skillId, LOG log) {
        sendAttackStatusPacketUpdate(type, value, skillId, log);
    }

    @Override
    protected void onIncreaseMp(TYPE type, int value, int skillId, LOG log) {
        // nothing todo
    }

    @Override
    protected void onReduceHp() {
        // nothing todo
    }

    @Override
    protected void onReduceMp() {
        // nothing todo
    }

    @Override
    public void triggerRestoreTask() {
        restoreLock.lock();
        try {
            if (lifeRestoreTask == null && !alreadyDead) {
                lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleHpRestoreTask(this);
            }
        } finally {
            restoreLock.unlock();
        }
    }

    public void startResting() {
        triggerRestoreTask();
    }
}
