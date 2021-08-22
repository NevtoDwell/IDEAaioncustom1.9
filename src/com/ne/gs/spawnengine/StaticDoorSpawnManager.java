/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import com.ne.gs.model.templates.staticdoor.DoorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.controllers.StaticObjectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.StaticDoor;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.staticdoor.StaticDoorTemplate;
import com.ne.gs.model.templates.staticdoor.StaticDoorWorld;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.PlayerAwareKnownList;

import java.util.List;

/**
 * @author MrPoke
 */
public final class StaticDoorSpawnManager {

    private static final Logger log = LoggerFactory.getLogger(StaticDoorSpawnManager.class);

    /**
     * @param instanceIndex
     */
    public static void spawnTemplate(int worldId, int instanceIndex) {
        StaticDoorWorld staticDoorWorld = DataManager.STATICDOOR_DATA.getStaticDoorWorlds(worldId);
        if (staticDoorWorld == null) {
            return;
        }
        int counter = 0;

        List<StaticDoorTemplate> tmps = staticDoorWorld.getStaticDoors();
        if(tmps == null){
            log.warn("Static doors for world: [" + worldId + "] not found ");
            return;
        }

        for (StaticDoorTemplate data : tmps) {
            if (data.getDoorType() != DoorType.DOOR) {
                // TODO: assign house doors to houses, so geo doors could be triggered by changing house settings;
                // The same for abyss doors, they need to have owners.
                continue;
            }
            SpawnTemplate spawn = new SpawnTemplate(new SpawnGroup2(worldId, 300001), data.getX(), data.getY(), data.getZ(), (byte) 0, 0, null, 0, 0);
            spawn.setStaticId(data.getDoorId());
            int objectId = IDFactory.getInstance().nextId();
            StaticDoor staticDoor = new StaticDoor(objectId, new StaticObjectController(), spawn, data, instanceIndex);
            staticDoor.setKnownlist(new PlayerAwareKnownList(staticDoor));
            bringIntoWorld(staticDoor, spawn, instanceIndex);
            if (staticDoor.getModel() != null) {

                if(staticDoor.isOpen())
                    staticDoor.getModel().deactivateAt(instanceIndex);
                else
                    staticDoor.getModel().activateAt(instanceIndex);
            }
            counter++;
        }
        if (counter > 0) {
            log.info("Spawned static doors: " + worldId + " [" + instanceIndex + "] : " + counter);
        }
    }

    /**
     * @param visibleObject
     * @param spawn
     * @param instanceIndex
     */
    private static void bringIntoWorld(VisibleObject visibleObject, SpawnTemplate spawn, int instanceIndex) {
        World world = World.getInstance();
        world.storeObject(visibleObject);
        world.setPosition(visibleObject, spawn.getWorldId(), instanceIndex, spawn.getX(), spawn.getY(), spawn.getZ(),
                spawn.getHeading());
        world.spawn(visibleObject);
    }
}
