/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world;

import com.ne.gs.instance.InstanceEngine;
import com.ne.gs.instance.handlers.InstanceHandler;

/**
 * @author ATracer
 */
public final class WorldMapInstanceFactory {

    /**
     * @param parent
     * @param instanceId
     *
     * @return
     */
    public static WorldMapInstance createWorldMapInstance(WorldMap parent, int instanceId) {
        return createWorldMapInstance(parent, instanceId, 0);
    }

    public static WorldMapInstance createWorldMapInstance(WorldMap parent, int instanceId, int ownerId) {
        WorldMapInstance worldMapInstance = null;
        if (parent.getMapId() == WorldMapType.RESHANTA.getId()) {
            worldMapInstance = new WorldMap3DInstance(parent, instanceId);
        } else {
            worldMapInstance = new WorldMap2DInstance(parent, instanceId, ownerId);
        }
        InstanceHandler instanceHandler = InstanceEngine.getInstance().getNewInstanceHandler(parent.getMapId());
        worldMapInstance.setInstanceHandler(instanceHandler);
        return worldMapInstance;
    }
    
    public static WorldMapInstance createEventWorldMapInstance(WorldMap parent, int instanceId, int eventHandlerId) {
        WorldMapInstance worldMapInstance = null;
        if (parent.getMapId() == WorldMapType.RESHANTA.getId()) {
            worldMapInstance = new WorldMap3DInstance(parent, instanceId);
        } else {
            worldMapInstance = new WorldMap2DInstance(parent, instanceId, 0);
        }
        InstanceHandler instanceHandler = InstanceEngine.getInstance().getNewEventInstanceHandler(eventHandlerId);
        worldMapInstance.setInstanceHandler(instanceHandler);
        return worldMapInstance;
    }
}
