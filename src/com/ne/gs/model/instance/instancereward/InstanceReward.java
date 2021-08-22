/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.instance.instancereward;

import javolution.util.FastList;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.InstanceScoreType;
import com.ne.gs.model.instance.playerreward.InstancePlayerReward;

/**
 * @author xTz
 */
public class InstanceReward<T extends InstancePlayerReward> {

    protected FastList<T> instanceRewards = new FastList<>();
    private InstanceScoreType instanceScoreType = InstanceScoreType.START_PROGRESS;
    protected Integer mapId;
    protected int instanceId;

    public InstanceReward(Integer mapId, int instanceId) {
        this.mapId = mapId;
        this.instanceId = instanceId;
    }

    public FastList<T> getInstanceRewards() {
        return instanceRewards;
    }

    public boolean containPlayer(Integer object) {
        for (InstancePlayerReward instanceReward : instanceRewards) {
            if (instanceReward.getOwner().equals(object)) {
                return true;
            }
        }
        return false;
    }

    public InstancePlayerReward getPlayerReward(Integer object) {
        for (InstancePlayerReward instanceReward : instanceRewards) {
            if (instanceReward.getOwner().equals(object)) {
                return instanceReward;
            }
        }
        return null;
    }

    public void addPlayerReward(T reward) {
        instanceRewards.add(reward);
    }

    public void setInstanceScoreType(InstanceScoreType instanceScoreType) {
        this.instanceScoreType = instanceScoreType;
    }

    public InstanceScoreType getInstanceScoreType() {
        return instanceScoreType;
    }

    public Integer getMapId() {
        return mapId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public FastList<InstancePlayerReward> getPlayersInside() {
        return getPlayersInsideByRace(Race.PC_ALL);
    }

    public FastList<InstancePlayerReward> getPlayersInsideByRace(Race race) {
        FastList<InstancePlayerReward> playerRewards = new FastList<>();
        for (InstancePlayerReward instanceReward : instanceRewards) {
            Player player = instanceReward.getPlayer();
            if (player != null && player.isOnline() && player.getInstanceId() == instanceId) {
                if (race == Race.PC_ALL || player.getRace().equals(race)) {
                    playerRewards.add(instanceReward);
                }
            }
        }
        return playerRewards;
    }

    public boolean isRewarded() {
        return instanceScoreType.isEndProgress();
    }

    public boolean isPreparing() {
        return instanceScoreType.isPreparing();
    }

    public boolean isStartProgress() {
        return instanceScoreType.isStartProgress();
    }

    public void clear() {
        instanceRewards.clear();
    }
}
