/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.assemblednpc;

import javolution.util.FastList;

/**
 * @author xTz
 */
public class AssembledNpc {

    private FastList<AssembledNpcPart> assembledPatrs = new FastList<>();
    private final long spawnTime = System.currentTimeMillis();
    private final int routeId;
    private final int mapId;

    public AssembledNpc(int routeId, int mapId, int liveTime, FastList<AssembledNpcPart> assembledPatrs) {
        this.assembledPatrs = assembledPatrs;
        this.routeId = routeId;
        this.mapId = mapId;
    }

    public FastList<AssembledNpcPart> getAssembledParts() {
        return assembledPatrs;
    }

    public int getRouteId() {
        return routeId;
    }

    public int getMapId() {
        return mapId;
    }

    public long getTimeOnMap() {
        return System.currentTimeMillis() - spawnTime;
    }
}
