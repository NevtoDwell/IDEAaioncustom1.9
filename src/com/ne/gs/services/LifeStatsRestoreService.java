/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.concurrent.Future;

import com.ne.gs.ai2.AIState;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.container.CreatureLifeStats;
import com.ne.gs.model.stats.container.PlayerLifeStats;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;

/**
 * @author ATracer
 */
public class LifeStatsRestoreService {

    private static final int DEFAULT_DELAY = 6000;
    private static final int DEFAULT_FPREDUCE_DELAY = 2000;
    private static final int DEFAULT_FPRESTORE_DELAY = 2000;

    private static final LifeStatsRestoreService instance = new LifeStatsRestoreService();

    /**
     * HP and MP restoring task
     *
     * @return Future<?>
     */
    public Future<?> scheduleRestoreTask(CreatureLifeStats<? extends Creature> lifeStats) {
        return ThreadPoolManager.getInstance().scheduleAtFixedRate(new HpMpRestoreTask(lifeStats), 1700, DEFAULT_DELAY);
    }

    /**
     * HP restoring task
     *
     * @param lifeStats
     *
     * @return
     */
    public Future<?> scheduleHpRestoreTask(CreatureLifeStats<? extends Creature> lifeStats) {
        return ThreadPoolManager.getInstance().scheduleAtFixedRate(new HpRestoreTask(lifeStats), 1700, DEFAULT_DELAY);
    }

    /**
     * @param lifeStats
     *
     * @return
     */
    public Future<?> scheduleFpReduceTask(PlayerLifeStats lifeStats, Integer costFp) {
        return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FpReduceTask(lifeStats, costFp), 2000, DEFAULT_FPREDUCE_DELAY);
    }

    /**
     * @param lifeStats
     *
     * @return
     */
    public Future<?> scheduleFpRestoreTask(PlayerLifeStats lifeStats) {
        return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FpRestoreTask(lifeStats), 2000, DEFAULT_FPRESTORE_DELAY);
    }

    public static LifeStatsRestoreService getInstance() {
        return instance;
    }

    private static class FpRestoreTask implements Runnable {

        private PlayerLifeStats lifeStats;

        private FpRestoreTask(PlayerLifeStats lifeStats) {
            this.lifeStats = lifeStats;
        }

        @Override
        public void run() {
            if (lifeStats.isAlreadyDead() || lifeStats.isFlyTimeFullyRestored()) {
                lifeStats.cancelFpRestore();
                lifeStats = null;
            } else {
                lifeStats.restoreFp();
            }
        }
    }

    private static class FpReduceTask implements Runnable {

        private PlayerLifeStats lifeStats;
        private final Integer costFp;

        private FpReduceTask(PlayerLifeStats lifeStats, Integer costFp) {
            this.lifeStats = lifeStats;
            this.costFp = costFp;
        }

        @Override
        public void run() {
            boolean inWorld = World.getInstance().isInWorld(lifeStats.getOwner());
            if (!inWorld || lifeStats.isAlreadyDead()) {
                lifeStats.cancelFpReduce();
                lifeStats = null;
                return;
            }

            if (lifeStats.getCurrentFp() == 0) {
                if (lifeStats.getOwner().getFlyState() > 0) {
                    lifeStats.getOwner().getFlyController().endFly(true);
                } else {
                    lifeStats.triggerFpRestore();
                }
            } else {
                int reduceFp = lifeStats.getOwner().getFlyState() == 2 && lifeStats.getOwner().isInsideZoneType(ZoneType.FLY) ? 1 : 2;
                if (costFp != null) {
                    reduceFp = costFp;
                }

                lifeStats.reduceFp(reduceFp);
                lifeStats.specialrestoreFp();
            }
        }
    }

    private static class HpMpRestoreTask implements Runnable {

        private CreatureLifeStats<?> lifeStats;

        private HpMpRestoreTask(CreatureLifeStats<?> lifeStats) {
            this.lifeStats = lifeStats;
        }

        @Override
        public void run() {
            boolean inWorld = World.getInstance().isInWorld(lifeStats.getOwner());
            if (!inWorld || lifeStats.isAlreadyDead() || lifeStats.isFullyRestoredHpMp()) {
                lifeStats.cancelRestoreTask();
                lifeStats = null;
            } else {
                lifeStats.restoreHp();
                lifeStats.restoreMp();
            }
        }
    }

    private static class HpRestoreTask implements Runnable {

        private CreatureLifeStats<?> lifeStats;

        private HpRestoreTask(CreatureLifeStats<?> lifeStats) {
            this.lifeStats = lifeStats;
        }

        @Override
        public void run() {
            boolean inWorld = World.getInstance().isInWorld(lifeStats.getOwner());
            if (!inWorld || lifeStats.isAlreadyDead() || lifeStats.isFullyRestoredHp() || lifeStats.getOwner().getAi2().getState().equals(AIState.FIGHT)) {
                lifeStats.cancelRestoreTask();
                lifeStats = null;
            } else {
                lifeStats.restoreHp();
            }
        }
    }
}
