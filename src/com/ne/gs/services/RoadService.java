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
import com.ne.gs.model.road.Road;
import com.ne.gs.model.templates.road.RoadTemplate;

/**
 * @author SheppeR
 */
public class RoadService {

    Logger log = LoggerFactory.getLogger(RoadService.class);

    private static final class SingletonHolder {

        protected static final RoadService instance = new RoadService();
    }

    public static RoadService getInstance() {
        return SingletonHolder.instance;
    }

    private RoadService() {
        for (RoadTemplate rt : DataManager.ROAD_DATA.getRoadTemplates()) {
            Road r = new Road(rt);
            r.spawn();
            log.debug("Added " + r.getName() + " at m=" + r.getWorldId() + ",x=" + r.getX() + ",y=" + r.getY() + ",z=" + r.getZ());
        }
    }
}
