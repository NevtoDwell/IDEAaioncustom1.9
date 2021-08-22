/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns.siegespawns;

import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.siege.SiegeSpawnType;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnSpotTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;

/**
 * @author xTz
 */
public class SiegeSpawnTemplate extends SpawnTemplate {

    private int siegeId;
    private SiegeRace siegeRace;
    private SiegeSpawnType siegeSpawnType;
    private SiegeModType siegeModType;

    public SiegeSpawnTemplate(SpawnGroup2 spawnGroup, SpawnSpotTemplate spot) {
        super(spawnGroup, spot);
    }

    public SiegeSpawnTemplate(SpawnGroup2 spawnGroup, float x, float y, float z, int heading, int randWalk, String walkerId,
                              int staticId, int fly) {
        super(spawnGroup, x, y, z, heading, randWalk, walkerId, staticId, fly);
    }

    public int getSiegeId() {
        return siegeId;
    }

    public SiegeRace getSiegeRace() {
        return siegeRace;
    }

    public SiegeSpawnType getSiegeSpawnType() {
        return siegeSpawnType;
    }

    public SiegeModType getSiegeModType() {
        return siegeModType;
    }

    public void setSiegeId(int siegeId) {
        this.siegeId = siegeId;
    }

    public void setSiegeRace(SiegeRace siegeRace) {
        this.siegeRace = siegeRace;
    }

    public void setSiegeSpawnType(SiegeSpawnType siegeSpawnType) {
        this.siegeSpawnType = siegeSpawnType;
    }

    public void setSiegeModType(SiegeModType siegeModType) {
        this.siegeModType = siegeModType;
    }

    public final boolean isPeace() {
        return siegeModType.equals(SiegeModType.PEACE);
    }

    public final boolean isSiege() {
        return siegeModType.equals(SiegeModType.SIEGE);
    }

    public final boolean isAssault() {
        return siegeModType.equals(SiegeModType.ASSAULT);
    }
}
