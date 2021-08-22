/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.configs.main.GeoDataConfig;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.CollisionDieActor;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.siege.SiegeShield;
import com.ne.gs.world.zone.ZoneInstance;
import javolution.util.FastMap;
import mw.engines.geo.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.shield.Shield;
import com.ne.gs.model.templates.shield.ShieldTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xavier
 */
public class ShieldService {

    Logger log = LoggerFactory.getLogger(ShieldService.class);

    private static final class SingletonHolder {

        protected static final ShieldService instance = new ShieldService();
    }

    private final FastMap<Integer, Shield> sphereShields = new FastMap<Integer, Shield>();
    private final FastMap<Integer, List<SiegeShield>> registeredShields = new FastMap<Integer, List<SiegeShield>>(0);

    public static ShieldService getInstance() {
        return SingletonHolder.instance;
    }

    private ShieldService() {
    }

    public ActionObserver createShieldObserver(SiegeShield geoShield, Creature observed) {
        ActionObserver observer = null;
        if (GeoDataConfig.GEO_SHIELDS_ENABLE) {
            observer = new CollisionDieActor(observed, geoShield.getGeometry());
            ((CollisionDieActor) observer).setEnabled(true);
        }
        return observer;
    }

    public void spawnAll() {
        for (Shield shield : sphereShields.values()) {
            shield.spawn();
            log.debug("Added " + shield.getName() + " at m=" + shield.getWorldId() + ",x=" + shield.getX() + ",y=" + shield.getY() + ",z="
                    + shield.getZ());
        }
        // TODO: check this list of not bound meshes (would remain inactive)
        for (List<SiegeShield> otherShields : registeredShields.values()) {
            for (SiegeShield shield : otherShields)
                log.debug("Not bound shield " + shield.getGeometry().toString());
        }
    }

    public void load(int mapId) {
        for (ShieldTemplate template : DataManager.SHIELD_DATA.getShieldTemplates()) {
            if (template.getMap() != mapId)
                continue;
            Shield f = new Shield(template);
            sphereShields.put(f.getId(), f);
        }
    }

    /**
     * Registers geo shield for zone lookup
     *
     * @param shield
     *          - shield to be registered
     */
    public void registerShield(int worldId, SiegeShield shield) {
        List<SiegeShield> mapShields = registeredShields.get(worldId);
        if (mapShields == null) {
            mapShields = new ArrayList<SiegeShield>();
            registeredShields.put(worldId, mapShields);
        }
        mapShields.add(shield);
    }

    /**
     * Attaches geo shield and removes obsolete sphere shield if such exists. Should be called when geo shields and
     * SiegeZoneInstance were created.
     *
     * @param location
     *          - siege location id
     */
    public void attachShield(SiegeLocation location) {
        List<SiegeShield> mapShields = registeredShields.get(location.getTemplate().getWorldId());
        if (mapShields == null)
            return;

        ZoneInstance zone = location.getZone().get(0);
        List<SiegeShield> shields = new ArrayList<SiegeShield>();

        for (int index = mapShields.size() - 1; index >= 0; index--) {
            SiegeShield shield = mapShields.get(index);
            Vector3f center = shield.getGeometry().getBoundingBox().getCenter();
            if (zone.getAreaTemplate().isInside3D(center.x, center.y, center.z)) {
                shields.add(shield);
                mapShields.remove(index);
                Shield sphereShield = sphereShields.get(location.getLocationId());
                if (sphereShield != null) {
                    sphereShields.remove(location.getLocationId());
                }
                shield.setSiegeLocationId(location.getLocationId());
            }
        }
        if (shields.size() == 0) {
            log.warn("Could not find a shield for locId: " + location.getLocationId());
        }
        else {
            location.setShields(shields);
        }
    }
}
