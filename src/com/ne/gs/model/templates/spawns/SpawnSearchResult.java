/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns;


/**
 * @author Rolandas
 */
public final class SpawnSearchResult {

    private final SpawnSpotTemplate spot;
    private final int worldId;

    public SpawnSearchResult(int worldId, SpawnSpotTemplate spot) {
        this.worldId = worldId;
        this.spot = spot;
    }

    public SpawnSpotTemplate getSpot() {
        return spot;
    }

    public int getWorldId() {
        return worldId;
    }
}
