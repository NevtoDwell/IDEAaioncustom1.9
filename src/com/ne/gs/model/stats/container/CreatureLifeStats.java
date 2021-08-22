/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.container;

import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.annotations.NotNull;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.services.LifeStatsRestoreService;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public abstract class CreatureLifeStats<T extends Creature> {

    private static final Logger log = LoggerFactory.getLogger(CreatureLifeStats.class);

    protected int currentHp;
    protected int currentMp;

    protected boolean alreadyDead = false;

    protected T owner;

    private final Lock hpLock = new ReentrantLock();
    private final Lock mpLock = new ReentrantLock();
    protected final Lock restoreLock = new ReentrantLock();

    protected volatile Future<?> lifeRestoreTask;

    public CreatureLifeStats(T owner, int currentHp, int currentMp) {
        this.owner = owner;
        this.currentHp = currentHp;
        this.currentMp = currentMp;
    }

    public T getOwner() {
        return owner;
    }

    public int getCurrentHp() {
        if(currentHp <=0 & !getOwner().getLifeStats().alreadyDead) {
            log.warn("CHECKPOINT: Current hp is lower than 0 "+ getOwner().getObjectId());
        }
        return currentHp;
    }

    public int getCurrentMp() {
        return currentMp;
    }

    /**
     * @return maxHp of creature according to stats
     */
    public int getMaxHp() {
        int maxHp = this.getOwner().getGameStats().getMaxHp().getCurrent();
        if (maxHp == 0) {
            maxHp = 1;
            log.warn("CHECKPOINT: maxhp is 0 :" + this.getOwner().getGameStats());
        }
        return maxHp;
    }

    /**
     * @return maxMp of creature according to stats
     */
    public int getMaxMp() {
        return this.getOwner().getGameStats().getMaxMp().getCurrent();
    }

    /**
     * @return the alreadyDead There is no setter method cause life stats should be completely renewed on revive
     */
    public boolean isAlreadyDead() {
        return alreadyDead;
    }

    /**
     * This method is called whenever caller wants to absorb creatures's HP
     *
     * @param value
     * @param attacker
     *
     * @return currentHp
     */
    public int reduceHp(int value, @NotNull Creature attacker) {
        boolean isDied = false;
        hpLock.lock();
        try {
            if (!alreadyDead) {
                int newHp = this.currentHp - value;

                if(newHp < 0)
                    newHp = 0;

                if (newHp == 0) {
                    alreadyDead = true;
                    isDied = true;
                }
                this.currentHp = newHp;
            }
        } finally {
            hpLock.unlock();
        }
        if (value != 0) {
            onReduceHp();
        }
        if (isDied) {
            getOwner().getController().onDie(attacker);
        }
        return currentHp;
    }

    /**
     * This method is called whenever caller wants to absorb creatures's HP
     *
     * @param value
     *
     * @return currentMp
     */
    public int reduceMp(int value) {
        if (getOwner() instanceof Player) {
            Player player = (Player) getOwner();
            if (player.isInvul()) { // не расходуется Mp в //invul 
                return 0;
            }
        }
        mpLock.lock();
        try {
            int newMp = this.currentMp - value;

            if (newMp < 0) {
                newMp = 0;
            }

            this.currentMp = newMp;
        } finally {
            mpLock.unlock();
        }
        if (value != 0) {
            onReduceMp();
        }
        return currentMp;
    }

    protected void sendAttackStatusPacketUpdate(TYPE type, int value, int skillId, LOG log) {
        if (owner == null) {
            return;
        }
        PacketSendUtility.broadcastPacketAndReceive(owner, new SM_ATTACK_STATUS(owner, type, skillId, value, log));
    }

    /**
     * This method is called whenever caller wants to restore creatures's HP
     *
     * @param value
     *
     * @return currentHp
     */
    public int increaseHp(TYPE type, int value) {
        return this.increaseHp(type, value, 0, LOG.REGULAR);
    }

    public int increaseHp(TYPE type, int value, int skillId, LOG log) {
        boolean hpIncreased = false;

        if (this.getOwner().getEffectController().isAbnormalSet(AbnormalState.DISEASE)) {
            return currentHp;
        }

        hpLock.lock();
        try {
            if (isAlreadyDead()) {
                return 0;
            }
            int newHp = this.currentHp + value;
            if (newHp > getMaxHp()) {
                newHp = getMaxHp();
            }
            if (currentHp != newHp) {
                this.currentHp = newHp;
                hpIncreased = true;
            }
        } finally {
            hpLock.unlock();
        }

        if (hpIncreased) {
            onIncreaseHp(type, value, skillId, log);
        }
        return currentHp;
    }

    /**
     * This method is called whenever caller wants to restore creatures's MP
     *
     * @param value
     *
     * @return currentMp
     */
    public int increaseMp(TYPE type, int value) {
        return this.increaseMp(type, value, 0, LOG.REGULAR);
    }

    public int increaseMp(TYPE type, int value, int skillId, LOG log) {
        boolean mpIncreased = false;
        mpLock.lock();
        try {
            if (isAlreadyDead()) {
                return 0;
            }
            int newMp = this.currentMp + value;
            if (newMp > getMaxMp()) {
                newMp = getMaxMp();
            }
            if (currentMp != newMp) {
                this.currentMp = newMp;
                mpIncreased = true;
            }
        } finally {
            mpLock.unlock();
        }

        if (mpIncreased) {
            onIncreaseMp(type, value, skillId, log);
        }
        return currentMp;
    }

    /**
     * Restores HP with value set as HP_RESTORE_TICK
     */
    public final void restoreHp() {
        increaseHp(TYPE.NATURAL_HP, getOwner().getGameStats().getHpRegenRate().getCurrent());
    }

    /**
     * Restores HP with value set as MP_RESTORE_TICK
     */
    public final void restoreMp() {
        increaseMp(TYPE.NATURAL_MP, getOwner().getGameStats().getMpRegenRate().getCurrent());
    }

    /**
     * Will trigger restore task if not already
     */
    public void triggerRestoreTask() {
        restoreLock.lock();
        try {
            if (lifeRestoreTask == null && !alreadyDead) {
                lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleRestoreTask(this);
            }
        } finally {
            restoreLock.unlock();
        }

    }

    /**
     * Cancel currently running restore task
     */
    public void cancelRestoreTask() {
        restoreLock.lock();
        try {
            if (lifeRestoreTask != null) {
                lifeRestoreTask.cancel(false);
                lifeRestoreTask = null;
            }
        } finally {
            restoreLock.unlock();
        }
    }

    /**
     * @return true or false
     */
    public boolean isFullyRestoredHpMp() {
        return getMaxHp() == currentHp && getMaxMp() == currentMp;
    }

    public boolean isFullyRestoredHp() {
        return getMaxHp() == currentHp;
    }

    /**
     * The purpose of this method is synchronize current HP and MP with updated MAXHP and MAXMP stats This method should be called only on creature load to game
     * or player level up
     */
    public void synchronizeWithMaxStats() {
        int maxHp = getMaxHp();
        if (currentHp != maxHp) {
            currentHp = maxHp;
        }
        int maxMp = getMaxMp();
        if (currentMp != maxMp) {
            currentMp = maxMp;
        }
    }

    /**
     * The purpose of this method is synchronize current HP and MP with MAXHP and MAXMP when max stats were decreased below current level
     */
    public void updateCurrentStats() {
        int maxHp = getMaxHp();
        if (maxHp < currentHp) {
            currentHp = maxHp;
        }

        int maxMp = getMaxMp();
        if (maxMp < currentMp) {
            currentMp = maxMp;
        }

        if (!isFullyRestoredHpMp()) {
            triggerRestoreTask();
        }
    }

    /**
     * @return HP percentage 0 - 100
     */
    public int getHpPercentage() {
        return (int) (100f * currentHp / getMaxHp());
    }

    protected abstract void onIncreaseMp(TYPE type, int value, int skillId, LOG log);

    protected abstract void onReduceMp();

    protected abstract void onIncreaseHp(TYPE type, int value, int skillId, LOG log);

    protected abstract void onReduceHp();

    /**
     * @param type
     * @param value
     *
     * @return
     */
    public int increaseFp(TYPE type, int value) {
        return 0;
    }

    public int getMaxFp() {
        return 0;
    }

    /**
     * @return
     */
    public int getCurrentFp() {
        return 0;
    }

	public int getMaxDp() {
		return 0;
	}

	public int getCurrentDp() {
		return 0;
	}

    /**
     * Cancel all tasks when player logout
     */
    public void cancelAllTasks() {
        cancelRestoreTask();
    }

    /**
     * This method can be used for Npc's to fully restore its HP and remove dead state of lifestats
     *
     * @param hpPercent
     */
    public void setCurrentHpPercent(int hpPercent) {
        hpLock.lock();
        try {
            int maxHp = getMaxHp();
            this.currentHp = (int) (maxHp * hpPercent / 100f);

            if (this.currentHp > 0) {
                this.alreadyDead = false;
            }
        } finally {
            hpLock.unlock();
        }
    }

    public void setCurrentHp(int hp) {
        boolean hpNotAtMaxValue = false;
        hpLock.lock();
        try {
            this.currentHp = hp;

            if (this.currentHp > 0) {
                this.alreadyDead = false;
            }

            if (this.currentHp < getMaxHp()) {
                hpNotAtMaxValue = true;
            }
        } finally {
            hpLock.unlock();
        }
        if (hpNotAtMaxValue) {
            onReduceHp();
        }
    }

    public int setCurrentMp(int value) {
        mpLock.lock();
        try {
            int newMp = value;

            if (newMp < 0) {
                newMp = 0;
            }

            this.currentMp = newMp;
        } finally {
            mpLock.unlock();
        }
        onReduceMp();
        return currentMp;
    }

    /**
     * This method can be used for Npc's to fully restore its MP
     *
     * @param mpPercent
     */
    public void setCurrentMpPercent(int mpPercent) {
        mpLock.lock();
        try {
            int maxMp = getMaxMp();
            this.currentMp = (int) (maxMp * mpPercent / 100f);
        } finally {
            mpLock.unlock();
        }
    }

}
