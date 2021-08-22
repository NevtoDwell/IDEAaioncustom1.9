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
public class InstancePlayerReward {

    private final Integer obj;
    private int points;
    private int playerPvPKills;
    private int playerMonsterKills;
    protected Player player;

    public InstancePlayerReward(Player player) {
        this.player = player;
        obj = player.getObjectId();
    }

    public Integer getOwner() {
        return obj;
    }

    public int getPoints() {
        return points;
    }

    public int getPvPKills() {
        return playerPvPKills;
    }

    public int getMonsterKills() {
        return playerMonsterKills;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void addPoints(int points) {
        this.points += points;
        if (this.points < 0) {
            this.points = 0;
        }
    }

    public void addPvPKillToPlayer() {
        playerPvPKills++;
    }

    public void addMonsterKillToPlayer() {
        playerMonsterKills++;
    }
}
