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
import java.util.concurrent.locks.ReentrantLock;

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_DP_INFO;
import com.ne.gs.network.aion.serverpackets.SM_FLY_TIME;
import com.ne.gs.network.aion.serverpackets.SM_STATUPDATE_DP;
import com.ne.gs.network.aion.serverpackets.SM_STATUPDATE_HP;
import com.ne.gs.network.aion.serverpackets.SM_STATUPDATE_MP;
import com.ne.gs.services.LifeStatsRestoreService;
import com.ne.gs.taskmanager.tasks.PacketBroadcaster.BroadcastMode;
import com.ne.gs.taskmanager.tasks.TeamEffectUpdater;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer, sphinx
 */
public class PlayerLifeStats extends CreatureLifeStats<Player> {

    protected int currentFp;
	protected int currentDp;
    private final ReentrantLock fpLock = new ReentrantLock();
	private final ReentrantLock dpLock = new ReentrantLock();

    private Future<?> flyRestoreTask;
    private Future<?> flyReduceTask;

    public PlayerLifeStats(Player owner) {
        super(owner, owner.getGameStats().getMaxHp().getCurrent(), owner.getGameStats().getMaxMp().getCurrent());
        currentFp = owner.getGameStats().getFlyTime().getCurrent();
	    currentDp = 0;
    }

    @Override
    protected void onReduceHp() {
        sendHpPacketUpdate();
        triggerRestoreTask();
        sendGroupPacketUpdate();
    }

    @Override
    protected void onReduceMp() {
        sendMpPacketUpdate();
        triggerRestoreTask();
        sendGroupPacketUpdate();
    }

    @Override
    protected void onIncreaseMp(TYPE type, int value, int skillId, LOG log) {
        if (value > 0) {
            sendMpPacketUpdate();
            sendAttackStatusPacketUpdate(type, value, skillId, log);
            sendGroupPacketUpdate();
        }
    }

    @Override
    protected void onIncreaseHp(TYPE type, int value, int skillId, LOG log) {
        if (isFullyRestoredHp()) {
            // FIXME: Temp Fix: Reset aggro list when hp is full.
            owner.getAggroList().clear();
        }
        if (value > 0) {
            sendHpPacketUpdate();
            sendAttackStatusPacketUpdate(type, value, skillId, log);
            sendGroupPacketUpdate();
        }
    }

    private void sendGroupPacketUpdate() {
        if (owner.isInTeam()) {
            TeamEffectUpdater.getInstance().startTask(owner);
        }
    }

    @Override
    public void synchronizeWithMaxStats() {
        if (isAlreadyDead()) {
            return;
        }

        super.synchronizeWithMaxStats();
        int maxFp = getMaxFp();
        if (currentFp != maxFp) {
            currentFp = maxFp;
        }
    }

    @Override
    public void updateCurrentStats() {
        super.updateCurrentStats();

        if (getMaxFp() < currentFp) {
            currentFp = getMaxFp();
        }

        if (!owner.isFlying() && !owner.isInSprintMode()) {
            triggerFpRestore();
        }
    }

    public void sendHpPacketUpdate() {
        owner.addPacketBroadcastMask(BroadcastMode.UPDATE_PLAYER_HP_STAT);
    }

    public void sendHpPacketUpdateImpl() {
        owner.sendPck(new SM_STATUPDATE_HP(currentHp, getMaxHp()));
    }

    public void sendMpPacketUpdate() {
        owner.addPacketBroadcastMask(BroadcastMode.UPDATE_PLAYER_MP_STAT);
    }

    public void sendMpPacketUpdateImpl() {
        owner.sendPck(new SM_STATUPDATE_MP(currentMp, getMaxMp()));
    }

	@Override
	public int getCurrentDp() {
		return currentDp;
	}

	@Override
	public int getMaxDp() {
		return owner.getGameStats().getMaxDp().getCurrent();
	}

	public int getDpPercentage() {
		return 100 * currentDp / getMaxDp();
	}

	public int setCurrentDp(int value) {

		if(owner.getPlayerClass().isStartingClass()) {
			return 0;
		}

		dpLock.lock();

		try {
			int newDp  = value > getMaxDp() ? getMaxDp() : value < 0 ? 0 : value;

			if(currentDp != newDp) {
				currentDp = newDp;
				onUpdateDp();
			}
		} finally {
			dpLock.unlock();
		}

		return currentDp;
	}

	public int increaseDp(int value) {

		if(owner.getPlayerClass().isStartingClass()) {
			return 0;
		}

		dpLock.lock();

		try {
			if(isAlreadyDead()) {
				return 0;
			}

			int newDp = currentDp + value;
			if(newDp > getMaxDp()) {
				newDp = getMaxDp();
			}

			if(currentDp != newDp) {
				currentDp = newDp;
				onUpdateDp();
			}
		} finally {
			dpLock.unlock();
		}

		return currentDp;
	}

	public int reduceDp(int value) {

		if(owner.getPlayerClass().isStartingClass()) {
			return 0;
		}

		dpLock.lock();

		try {
			int newDp = currentDp - value;
			if(newDp < 0) {
				newDp = 0;
			}

			if(currentDp != newDp) {
				currentDp = newDp;
				onUpdateDp();
			}
		} finally {
			dpLock.unlock();
		}

		return currentDp;
	}

	protected void onUpdateDp() {
		owner.addPacketBroadcastMask(BroadcastMode.UPDATE_PLAYER_DP_STAT);
		PacketSendUtility.broadcastPacket(owner, new SM_DP_INFO(owner.getObjectId(), currentDp), true);
		//owner.getGameStats().updateStatsAndSpeedVisually();
	}

    /**
     * @return the currentFp
     */
    @Override
    public int getCurrentFp() {
        return currentFp;
    }

    @Override
    public int getMaxFp() {
        return owner.getGameStats().getFlyTime().getCurrent();
    }

    /**
     * @return FP percentage 0 - 100
     */
    public int getFpPercentage() {
        return 100 * currentFp / getMaxFp();
    }

    /**
     * This method is called whenever caller wants to restore creatures's FP
     *
     * @param value
     *
     * @return
     */
    @Override
    public int increaseFp(TYPE type, int value) {
        return this.increaseFp(type, value, 0, LOG.REGULAR);
    }

    public int increaseFp(TYPE type, int value, int skillId, LOG log) {
        fpLock.lock();

        try {
            if (isAlreadyDead()) {
                return 0;
            }
            int newFp = currentFp + value;
            if (newFp > getMaxFp()) {
                newFp = getMaxFp();
            }
            if (currentFp != newFp) {
                onIncreaseFp(type, newFp - currentFp, skillId, log);
                currentFp = newFp;
            }
        } finally {
            fpLock.unlock();
        }

        return currentFp;

    }

    /**
     * This method is called whenever caller wants to reduce creatures's MP
     *
     * @param value
     *
     * @return
     */
    public int reduceFp(int value) {
        fpLock.lock();
        try {
            int newFp = currentFp - value;

            if (newFp < 0) {
                newFp = 0;
            }

            currentFp = newFp;
        } finally {
            fpLock.unlock();
        }

        onReduceFp();

        return currentFp;
    }

    public int setCurrentFp(int value) {
        fpLock.lock();
        try {
            int newFp = value;

            if (newFp < 0) {
                newFp = 0;
            }

            currentFp = newFp;
        } finally {
            fpLock.unlock();
        }

        onReduceFp();

        return currentFp;
    }

    protected void onIncreaseFp(TYPE type, int value, int skillId, LOG log) {
        if (value > 0) {
            sendAttackStatusPacketUpdate(type, value, skillId, log);
            owner.addPacketBroadcastMask(BroadcastMode.UPDATE_PLAYER_FLY_TIME);
        }
    }

    protected void onReduceFp() {
        owner.addPacketBroadcastMask(BroadcastMode.UPDATE_PLAYER_FLY_TIME);
    }

    public void sendFpPacketUpdateImpl() {
        if (owner == null) {
            return;
        }

        owner.sendPck(new SM_FLY_TIME(currentFp, getMaxFp()));
    }

	public void sendDpPacketUpdateImpl() {
		if(owner == null) {
			return;
		}

		owner.sendPck(new SM_STATUPDATE_DP(currentDp));
	}

    /**
     * this method should be used only on FlyTimeRestoreService
     */
    public void restoreFp() {
        // how much fly time restoring per 2 second.
        increaseFp(TYPE.NATURAL_FP, 1);
    }

    public void specialrestoreFp() {
        if (owner.getGameStats().getStat(StatEnum.REGEN_FP, 0).getCurrent() != 0) {
            increaseFp(TYPE.NATURAL_FP, owner.getGameStats().getStat(StatEnum.REGEN_FP, 0).getCurrent() / 3);
        }
    }

    public void triggerFpRestore() {
        cancelFpReduce();

        restoreLock.lock();
        try {
            if (flyRestoreTask == null && !alreadyDead && !isFlyTimeFullyRestored()) {
                flyRestoreTask = LifeStatsRestoreService.getInstance().scheduleFpRestoreTask(this);
            }
        } finally {
            restoreLock.unlock();
        }
    }

    public void cancelFpRestore() {
        restoreLock.lock();
        try {
            if (flyRestoreTask != null && !flyRestoreTask.isCancelled()) {
                flyRestoreTask.cancel(false);
                flyRestoreTask = null;
            }
        } finally {
            restoreLock.unlock();
        }
    }

    public void triggerFpReduceByCost(Integer costFp) {
        triggerFpReduce(costFp);
    }

    public void triggerFpReduce() {
        triggerFpReduce(null);
    }

    private void triggerFpReduce(Integer costFp) {
        cancelFpRestore();
        restoreLock.lock();
        try {
            if (flyReduceTask == null && !alreadyDead && owner.getAccessLevel() < AdminConfig.GM_FLIGHT_UNLIMITED && !owner.isUnderNoFPConsum()) {
                flyReduceTask = LifeStatsRestoreService.getInstance().scheduleFpReduceTask(this, costFp);
            }
        } finally {
            restoreLock.unlock();
        }
    }

    public void cancelFpReduce() {
        restoreLock.lock();
        try {
            if (flyReduceTask != null && !flyReduceTask.isCancelled()) {
                flyReduceTask.cancel(false);
                flyReduceTask = null;
            }
        } finally {
            restoreLock.unlock();
        }
    }

    public boolean isFlyTimeFullyRestored() {
        return getMaxFp() == currentFp;
    }

    @Override
    public void cancelAllTasks() {
        super.cancelAllTasks();
        cancelFpReduce();
        cancelFpRestore();
    }

    public void triggerRestoreOnRevive() {
        triggerRestoreTask();
        triggerFpRestore();
    }
}
