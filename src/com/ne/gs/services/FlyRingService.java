/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.flyring.FlyRing;
import com.ne.gs.model.templates.flyring.FlyRingTemplate;

/**
 * @author xavier
 */
public class FlyRingService {

    Logger log = LoggerFactory.getLogger(FlyRingService.class);

    private static final class SingletonHolder {

        protected static final FlyRingService instance = new FlyRingService();
    }

    public static FlyRingService getInstance() {
        return SingletonHolder.instance;
    }

    private FlyRingService() {
        for (FlyRingTemplate t : DataManager.FLY_RING_DATA.getFlyRingTemplates()) {
            FlyRing f = new FlyRing(t, 0);
            f.spawn();
            log.debug("Added " + f.getName() + " at m=" + f.getWorldId() + ",x=" + f.getX() + ",y=" + f.getY() + ",z=" + f.getZ());
        }
    }
}
