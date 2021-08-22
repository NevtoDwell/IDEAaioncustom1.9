/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import com.ne.gs.ai2.AbstractAI;
import com.ne.gs.ai2.eventcallback.OnDieEventCallback;
import com.ne.gs.services.SiegeService;

public class SiegeBossDeathListener extends OnDieEventCallback {
    private final Siege<?> siege;

    public SiegeBossDeathListener(Siege siege) {
        this.siege = siege;
    }

    @Override
    public void onDie(AbstractAI ai) {
        siege.setBossKilled(true);
        SiegeService.getInstance().stopSiege(siege.getSiegeLocationId());
    }
}
