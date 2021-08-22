/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import com.ne.gs.services.SiegeService;

public class SiegeStartRunnable implements Runnable {

    private final int locationId;

    public SiegeStartRunnable(int locationId) {
        this.locationId = locationId;
    }

    @Override
    public void run() {
        SiegeService.getInstance().checkSiegeStart(getLocationId());
    }

    public int getLocationId() {
        return locationId;
    }
}
