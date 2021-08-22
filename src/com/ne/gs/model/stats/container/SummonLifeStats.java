/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.container;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.ne.gs.services.LifeStatsRestoreService;

/**
 * @author ATracer
 */
public class SummonLifeStats extends CreatureLifeStats<Summon> {

    public SummonLifeStats(Summon owner) {
        super(owner, owner.getGameStats().getMaxHp().getCurrent(), owner.getGameStats().getMaxMp().getCurrent());
    }

    @Override
    protected void onIncreaseHp(TYPE type, int value, int skillId, LOG log) {
        Creature master = getOwner().getMaster();
        sendAttackStatusPacketUpdate(type, value, skillId, log);

        if (master instanceof Player) {
            ((Player) master).sendPck(new SM_SUMMON_UPDATE(getOwner()));
        }
    }

    @Override
    protected void onIncreaseMp(TYPE type, int value, int skillId, LOG log) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onReduceHp() {
        Creature master = getOwner().getMaster();
        if (master instanceof Player) {
            ((Player) master).sendPck(new SM_SUMMON_UPDATE(getOwner()));
        }
    }

    @Override
    protected void onReduceMp() {
        // TODO Auto-generated method stub
    }

    @Override
    public Summon getOwner() {
        return super.getOwner();
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
}
