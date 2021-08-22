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
public class DredgionPlayerReward extends InstancePlayerReward {

    private int zoneCaptured;

    public DredgionPlayerReward(Player player) {
        super(player);
    }

    public void captureZone() {
        zoneCaptured++;
    }

    public int getZoneCaptured() {
        return zoneCaptured;
    }
}
