/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.services.abyss.AbyssPointsService;

/**
 * @author hex1r0
 */
public class AbyssPointsListener extends AbyssPointsService.ApAddCallback {
    private final Siege<?> siege;

    public AbyssPointsListener(Siege<?> siege) {
        this.siege = siege;
    }

    @Override
    public void onApAdd(Player player, VisibleObject vo, int points, Class rewarder) {

        if(points <= 0)
            return;

        SiegeLocation fortress = siege.getSiegeLocation();

        if (fortress.isInsideLocation(player) && (vo instanceof SiegeNpc || vo instanceof Player)) {
            siege.addAbyssPoints(player, points);
        }
    }
}
