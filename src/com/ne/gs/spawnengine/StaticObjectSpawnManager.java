/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import com.ne.gs.controllers.StaticObjectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.StaticObject;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.VisibleObjectTemplate;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.PlayerAwareKnownList;

/**
 * @author ATracer
 */
public final class StaticObjectSpawnManager {

    /**
     * @param instanceIndex
     */
    public static void spawnTemplate(SpawnGroup2 spawn, int instanceIndex) {
        VisibleObjectTemplate objectTemplate = DataManager.ITEM_DATA.getItemTemplate(spawn.getNpcId());
        if (objectTemplate == null) {
            return;
        }

        if (spawn.hasPool()) {
            for (int i = 0; i < spawn.getPool(); i++) {
                SpawnTemplate template = spawn.getRndTemplate();
                int objectId = IDFactory.getInstance().nextId();
                StaticObject staticObject = new StaticObject(objectId, new StaticObjectController(), template, objectTemplate);
                staticObject.setKnownlist(new PlayerAwareKnownList(staticObject));
                bringIntoWorld(staticObject, template, instanceIndex);
            }
        } else {
            for (SpawnTemplate template : spawn.getSpawnTemplates()) {
                int objectId = IDFactory.getInstance().nextId();
                StaticObject staticObject = new StaticObject(objectId, new StaticObjectController(), template, objectTemplate);
                staticObject.setKnownlist(new PlayerAwareKnownList(staticObject));
                bringIntoWorld(staticObject, template, instanceIndex);
            }
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
        world.setPosition(visibleObject, spawn.getWorldId(), instanceIndex, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading());
        world.spawn(visibleObject);
    }
}
