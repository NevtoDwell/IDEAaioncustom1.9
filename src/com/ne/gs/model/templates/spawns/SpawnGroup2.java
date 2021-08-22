/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns;

import java.util.ArrayList;
import java.util.List;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.ne.gs.spawnengine.SpawnHandlerType;

/**
 * @author xTz
 * @modified Rolandas
 */
public class SpawnGroup2 {

    private final int worldId;
    private int npcId;
    private int pool;
    private int difficultId;
    private TemporarySpawn temporarySpawn;
    private int respawnTime;
    private SpawnHandlerType handlerType;
    private final List<SpawnTemplate> spots = new ArrayList<>();

    public SpawnGroup2(int worldId, Spawn spawn) {
        this.worldId = worldId;
        initializing(spawn);
        for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
            SpawnTemplate spawnTemplate = new SpawnTemplate(this, template);
            if (spawn.isEventSpawn()) {
                spawnTemplate.setEventTemplate(spawn.getEventTemplate());
            }
            spots.add(spawnTemplate);
        }
    }

    public SpawnGroup2(int worldId, Spawn spawn, int siegeId, SiegeRace race, SiegeModType mod) {
        this.worldId = worldId;
        initializing(spawn);
        for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
            SiegeSpawnTemplate spawnTemplate = new SiegeSpawnTemplate(this, template);
            spawnTemplate.setSiegeId(siegeId);
            spawnTemplate.setSiegeRace(race);
            spawnTemplate.setSiegeModType(mod);
            spots.add(spawnTemplate);
        }
    }

    private void initializing(Spawn spawn) {
        temporarySpawn = spawn.getTemporarySpawn();
        respawnTime = spawn.getRespawnTime();
        pool = spawn.getPool();
        npcId = spawn.getNpcId();
        handlerType = spawn.getSpawnHandlerType();
        difficultId = spawn.getDifficultId();
    }

    public SpawnGroup2(int worldId, int npcId) {
        this.worldId = worldId;
        this.npcId = npcId;
    }

    public List<SpawnTemplate> getSpawnTemplates() {
        return spots;
    }

    public void addSpawnTemplate(SpawnTemplate spawnTemplate) {
        spots.add(spawnTemplate);
    }

    public int getWorldId() {
        return worldId;
    }

    public int getNpcId() {
        return npcId;
    }

    public TemporarySpawn geTemporarySpawn() {
        return temporarySpawn;
    }

    public int getPool() {
        return pool;
    }

    public boolean hasPool() {
        return pool > 0;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public void setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
    }

    public boolean isTemporarySpawn() {
        return temporarySpawn != null;
    }

    public SpawnHandlerType getHandlerType() {
        return handlerType;
    }

    public synchronized SpawnTemplate getRndTemplate() {
        List<SpawnTemplate> templates = new ArrayList<>();
        for (SpawnTemplate template : spots) {
            if (!template.isUsed()) {
                templates.add(template);
            }
        }
        SpawnTemplate spawnTemplate = templates.get(Rnd.get(0, templates.size() - 1));
        spawnTemplate.setUse(true);
        return spawnTemplate;
    }

    public int getDifficultId() {
        return difficultId;
    }
}
