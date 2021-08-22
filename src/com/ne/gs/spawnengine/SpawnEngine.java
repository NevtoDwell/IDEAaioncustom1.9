/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.math.AionPos;
import com.ne.commons.network.util.ThreadPoolManager;
import com.ne.gs.configs.administration.DeveloperConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Gatherable;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.ne.gs.model.templates.world.WorldMapTemplate;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * This class is responsible for NPCs spawn management. Current implementation is temporal and will be replaced in the future.
 *
 * @author Luno modified by ATracer, Source, Wakizashi, xTz, nrg
 */
public final class SpawnEngine {

    private static final Logger log = LoggerFactory.getLogger(SpawnEngine.class);
    private static boolean initialLoad = false;

    /**
     * Creates VisibleObject instance and spawns it using given {@link SpawnTemplate} instance.
     *
     * @param spawn
     *
     * @return created and spawned VisibleObject
     */
    public static VisibleObject spawnObject(SpawnTemplate spawn, int instanceIndex) {
        final VisibleObject visObj = getSpawnedObject(spawn, instanceIndex);
        if (spawn.isEventSpawn()) {
            spawn.getEventTemplate().addSpawnedObject(visObj);
        }

        if (visObj != null) {
            if (initialLoad) {
                visObj.setIsNewSpawn(false);
            } else {
                ThreadPoolManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        visObj.setIsNewSpawn(false);
                    }

                }, 1000);
            }
        }

        spawn.setVisibleObject(visObj);
        return visObj;
    }

    private static VisibleObject getSpawnedObject(SpawnTemplate spawn, int instanceIndex) {
        int objectId = spawn.getNpcId();

        if (objectId > 400000 && objectId < 499999) {
            return VisibleObjectSpawner.spawnGatherable(spawn, instanceIndex);
        } else if (spawn instanceof SiegeSpawnTemplate) {
            return VisibleObjectSpawner.spawnSiegeNpc((SiegeSpawnTemplate) spawn, instanceIndex);
        } else {
            return VisibleObjectSpawner.spawnNpc(spawn, instanceIndex);
        }
    }

    /**
     * @param worldId
     * @param npcId
     * @param x
     * @param y
     * @param z
     * @param heading
     *
     * @return
     */
    static SpawnTemplate createSpawnTemplate(int worldId, int npcId, float x, float y, float z, int heading) {
        return new SpawnTemplate(new SpawnGroup2(worldId, npcId), x, y, z, heading, 0, null, 0, 0);
    }

    /**
     * Should be used when you need to add a siegespawn through code and not from static_data spawns (e.g. CustomBalaurAssault)
     */
    static SpawnTemplate createSpawnTemplate(int worldId, int npcId, float x, float y, float z, byte heading,
                                             int creatorId, String masterName) {
        SpawnTemplate template = createSpawnTemplate(worldId, npcId, x, y, z, heading);
        template.setCreatorId(creatorId);
        template.setMasterName(masterName);
        return template;
    }

    public static SiegeSpawnTemplate addNewSiegeSpawn(int worldId, int npcId, int siegeId, SiegeRace race, SiegeModType mod,
                                                      float x, float y, float z, int heading) {
        SiegeSpawnTemplate spawnTemplate = new SiegeSpawnTemplate(new SpawnGroup2(worldId, npcId), x, y, z, heading, 0, null, 0, 0);

        spawnTemplate.setSiegeId(siegeId);
        spawnTemplate.setSiegeRace(race);
        spawnTemplate.setSiegeModType(mod);
        return spawnTemplate;
    }

    /**
     * Should be used when need to define whether spawn will be deleted after death Using this method spawns will not be saved with //save_spawn command
     *
     * @param worldId
     * @param npcId
     * @param x
     * @param y
     * @param z
     * @param heading
     * @param respawnTime
     *
     * @return SpawnTemplate
     */
    public static SpawnTemplate addNewSpawn(int worldId, int npcId, float x, float y, float z, int heading,
                                            int respawnTime) {
        SpawnTemplate spawnTemplate = createSpawnTemplate(worldId, npcId, x, y, z, heading);
        spawnTemplate.setRespawnTime(respawnTime);
        return spawnTemplate;
    }

    public static SpawnTemplate addNewSpawn(int npcId, AionPos pos, int respawnTime) {
        return addNewSpawn(pos.getMapId(), npcId, pos.getX(), pos.getY(), pos.getZ(), (byte) pos.getH(), respawnTime);
    }

    /**
     * Create non-permanent spawn template with no respawn
     *
     * @param worldId
     * @param npcId
     * @param x
     * @param y
     * @param z
     * @param heading
     *
     * @return
     */
    public static SpawnTemplate addNewSingleTimeSpawn(int worldId, int npcId, float x, float y, float z, int heading) {
        return addNewSpawn(worldId, npcId, x, y, z, heading, 0);
    }

    public static SpawnTemplate addNewSingleTimeSpawn(int npcId, AionPos pos) {
        return addNewSpawn(pos.getMapId(), npcId, pos.getX(), pos.getY(), pos.getZ(), (byte) pos.getH(), 0);
    }

    public static SpawnTemplate addNewSingleTimeSpawn(int worldId, int npcId, float x, float y, float z, byte heading,
                                                      int creatorId, String masterName) {
        SpawnTemplate template = addNewSpawn(worldId, npcId, x, y, z, heading, 0);
        template.setCreatorId(creatorId);
        template.setMasterName(masterName);
        return template;
    }

    public static void bringIntoWorld(VisibleObject visibleObject, SpawnTemplate spawn, int instanceIndex) {
        bringIntoWorld(visibleObject, spawn.getWorldId(), instanceIndex, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading());
    }

    public static void bringIntoWorld(VisibleObject visibleObject, int worldId, int instanceIndex, float x, float y,
                                      float z, int h) {
        World world = World.getInstance();
        world.storeObject(visibleObject);
        world.setPosition(visibleObject, worldId, instanceIndex, x, y, z, h);
        world.spawn(visibleObject);
    }

    public static void bringIntoWorld(VisibleObject visibleObject) {
        if (visibleObject.getPosition() == null) {
            throw new IllegalArgumentException("Position is null");
        }
        World world = World.getInstance();
        world.storeObject(visibleObject);
        world.spawn(visibleObject);
    }

    /**
     * Spawn all NPC's from templates
     */
    public static void spawnAll() {
        if (!DeveloperConfig.SPAWN_ENABLE) {
            log.info("Spawns are disabled");
            return;
        }

        try {
            initialLoad = true;
            for (WorldMapTemplate worldMapTemplate : DataManager.WORLD_MAPS_DATA) {
                if (worldMapTemplate.isInstance()) {
                    continue;
                }
                spawnBasedOnTemplate(worldMapTemplate);
            }
            initialLoad = false;
            DataManager.SPAWNS_DATA2.clearTemplates();
            printWorldSpawnStats();
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    /**
     * @param worldId
     */
    public static void spawnWorldMap(int worldId) {
        WorldMapTemplate template = DataManager.WORLD_MAPS_DATA.getTemplate(worldId);
        if (template != null && !template.isInstance()) {
            spawnBasedOnTemplate(template);
        }
    }

    /**
     * @param worldMapTemplate
     */
    private static void spawnBasedOnTemplate(WorldMapTemplate worldMapTemplate) {
        int maxTwin = worldMapTemplate.getTwinCount();
        int mapId = worldMapTemplate.getMapId();
        int numberToSpawn = maxTwin > 0 ? maxTwin : 1;

        for (int instanceId = 1; instanceId <= numberToSpawn; instanceId++) {
            spawnInstance(mapId, instanceId, 0);
        }
    }

    public static void spawnInstance(int worldId, int instanceId, int difficultId) {
        spawnInstance(worldId, instanceId, difficultId, 0);
    }

    /**
     * @param worldId
     * @param instanceId
     */
    public static void spawnInstance(int worldId, int instanceId, int difficultId, int ownerId) {
        List<SpawnGroup2> worldSpawns = DataManager.SPAWNS_DATA2.getSpawnsByWorldId(worldId);
        StaticDoorSpawnManager.spawnTemplate(worldId, instanceId);

        int spawnedCounter = 0;
        if (worldSpawns != null) {
            for (SpawnGroup2 spawn : worldSpawns) {
                int difficult = spawn.getDifficultId();
                if ((difficult == 0) || (difficult == difficultId)) {
                    if (spawn.isTemporarySpawn()) {
                        TemporarySpawnEngine.addSpawnGroup(spawn);
                    } else if (spawn.getHandlerType() != null) {
                        switch (spawn.getHandlerType()) {
                            case RIFT:
                                RiftSpawnManager.addRiftSpawnTemplate(spawn);
                                break;
                            case STATIC:
                                StaticObjectSpawnManager.spawnTemplate(spawn, instanceId);
                            default:
                                break;
                        }
                    } else if (spawn.hasPool()) {
                        for (int i = 0; i < spawn.getPool(); i++) {
                            SpawnTemplate template = spawn.getRndTemplate();
                            spawnObject(template, instanceId);
                            spawnedCounter++;
                        }
                    } else {
                        for (SpawnTemplate template : spawn.getSpawnTemplates()) {
                            spawnObject(template, instanceId);
                            spawnedCounter++;
                        }
                    }
                }
            }
            WalkerFormator.getInstance().organizeAndSpawn();
        }
        log.info("Spawned " + worldId + " [" + instanceId + "] : " + spawnedCounter);
    }

    public static void printWorldSpawnStats() {
        StatsCollector visitor = new StatsCollector();
        World.getInstance().doOnAllObjects(visitor);
        log.info("Loaded " + visitor.getNpcCount() + " npc spawns");
        log.info("Loaded " + visitor.getGatherableCount() + " gatherable spawns");
    }

    static class StatsCollector implements Visitor<VisibleObject> {

        int npcCount;
        int gatherableCount;

        @Override
        public void visit(VisibleObject object) {
            if (object instanceof Npc) {
                npcCount++;
            } else if (object instanceof Gatherable) {
                gatherableCount++;
            }
        }

        public int getNpcCount() {
            return npcCount;
        }

        public int getGatherableCount() {
            return gatherableCount;
        }
    }
}
