/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.instance.playerreward;

import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public class CruciblePlayerReward extends InstancePlayerReward {

    private int spawnPosition;
    private boolean isRewarded = false;
    private int insignia;
    private boolean isPlayerLeave = false;
    private boolean isPlayerDefeated = false;

    public CruciblePlayerReward(Player player) {
        super(player);
    }

    public boolean isRewarded() {
        return isRewarded;
    }

    public void setRewarded() {
        isRewarded = true;
    }

    public void setInsignia(int insignia) {
        this.insignia = insignia;
    }

    public int getInsignia() {
        return insignia;
    }

    public void setSpawnPosition(int spawnPosition) {
        this.spawnPosition = spawnPosition;
    }

    public int getSpawnPosition() {
        return spawnPosition;
    }

    public boolean isPlayerLeave() {
        return isPlayerLeave;
    }

    public void setPlayerLeave() {
        isPlayerLeave = true;
    }

    public void setPlayerDefeated(boolean value) {
        isPlayerDefeated = value;
    }

    public boolean isPlayerDefeated() {
        return isPlayerDefeated;
    }
}
