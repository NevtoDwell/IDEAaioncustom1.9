/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2;

import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class PlayerTeamMember implements TeamMember<Player> {

    final Player player;

    private long lastOnlineTime;

    public PlayerTeamMember(Player player) {
        this.player = player;
    }

    @Override
    public Integer getObjectId() {
        return player.getObjectId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Player getObject() {
        return player;
    }

    public long getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void updateLastOnlineTime() {
        lastOnlineTime = System.currentTimeMillis();
    }

    public boolean isOnline() {
        return player.isOnline();
    }

    public float getX() {
        return player.getX();
    }

    public float getY() {
        return player.getY();
    }

    public float getZ() {
        return player.getZ();
    }

    public int getHeading() {
        return player.getHeading();
    }

    public byte getLevel() {
        return player.getLevel();
    }
}
