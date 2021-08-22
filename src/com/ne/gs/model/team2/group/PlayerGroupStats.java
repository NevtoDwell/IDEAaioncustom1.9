/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class PlayerGroupStats implements Predicate<Player> {

    private final PlayerGroup group;
    private int minExpPlayerLevel;
    private int maxExpPlayerLevel;

    Player minLevelPlayer;
    Player maxLevelPlayer;

    PlayerGroupStats(PlayerGroup group) {
        this.group = group;
    }

    public void onAddPlayer(PlayerGroupMember member) {
        group.applyOnMembers(this);
        calculateExpLevels();
    }

    public void onRemovePlayer(PlayerGroupMember member) {
        group.applyOnMembers(this);
    }

    private void calculateExpLevels() {
        minExpPlayerLevel = minLevelPlayer.getLevel();
        maxExpPlayerLevel = maxLevelPlayer.getLevel();
        minLevelPlayer = null;
        maxLevelPlayer = null;
    }

    @Override
    public boolean apply(Player player) {
        if (minLevelPlayer == null || maxLevelPlayer == null) {
            minLevelPlayer = player;
            maxLevelPlayer = player;
        } else {
            if (player.getCommonData().getExp() < minLevelPlayer.getCommonData().getExp()) {
                minLevelPlayer = player;
            }
            if (!player.isMentor() && player.getCommonData().getExp() > maxLevelPlayer.getCommonData().getExp()) {
                maxLevelPlayer = player;
            }
        }
        return true;
    }

    public int getMinExpPlayerLevel() {
        return minExpPlayerLevel;
    }

    public int getMaxExpPlayerLevel() {
        return maxExpPlayerLevel;
    }

}
