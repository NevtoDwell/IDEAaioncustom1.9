/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import com.ne.gs.configs.main.GeoDataConfig;
import com.ne.gs.model.geometry.*;
import com.ne.gs.model.siege.SiegeShield;
import com.ne.gs.model.templates.materials.MaterialTemplate;
import com.ne.gs.model.templates.zone.MaterialZoneTemplate;
import com.ne.gs.model.templates.zone.WorldZoneTemplate;
import com.ne.gs.services.ShieldService;
import com.ne.gs.world.zone.handler.*;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastMap;
import mw.engines.geo.scene.AionMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.scripting.classlistener.AggregatedClassListener;
import com.ne.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.ne.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.ne.commons.scripting.scriptmanager.ScriptManager;
import com.ne.gs.GameServerError;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.GameEngine;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.templates.zone.ZoneInfo;

/**
 * @author ATracer modified by antness
 */
public final class ZoneService implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(ZoneService.class);
    private final Map<ZoneName, Class<? extends ZoneHandler>> handlers = new HashMap<>();
    public static final ZoneHandler DUMMY_ZONE_HANDLER = new GeneralZoneHandler();
    private final FastMap<ZoneName, ZoneHandler> collidableHandlers = new FastMap<ZoneName, ZoneHandler>();
    private TIntObjectHashMap<List<ZoneInfo>> zoneByMapIdMap;
    private static ScriptManager scriptManager = new ScriptManager();
    public static final File ZONE_DESCRIPTOR_FILE = new File("./data/scripts/system/zonehandlers.xml");

    private ZoneService() {
        this.zoneByMapIdMap = DataManager.ZONE_DATA.getZones();
    }

    public static ZoneService getInstance() {
        return SingletonHolder.instance;
    }

    public ZoneHandler getNewZoneHandler(ZoneName zoneName) {

        ZoneHandler zoneHandler = collidableHandlers.get(zoneName);
        if (zoneHandler != null)
            return zoneHandler;

        Class<? extends ZoneHandler> zoneClass = handlers.get(zoneName);
        if (zoneClass != null) {
            try {
                zoneHandler = zoneClass.newInstance();
            } catch (IllegalAccessException ex) {
                log.warn("Can't instantiate zone handler " + zoneName, ex);
            } catch (Exception ex) {
                log.warn("Can't instantiate zone handler " + zoneName, ex);
            }
        }
        if (zoneHandler == null) {
            zoneHandler = DUMMY_ZONE_HANDLER;
        }
        return zoneHandler;
    }

    /**
     * @param handler
     */
    public final void addZoneHandlerClass(Class<? extends ZoneHandler> handler) {
        ZoneNameAnnotation idAnnotation = handler.getAnnotation(ZoneNameAnnotation.class);
        if (idAnnotation != null) {
            String[] zoneNames = idAnnotation.value().split(" ");
            for (String zoneNameString : zoneNames) {
                try {
                    ZoneName zoneName = ZoneName.get(zoneNameString.trim());
                    if (zoneName == ZoneName.get("NONE")) {
                        throw new RuntimeException();
                    }
                    handlers.put(zoneName, handler);
                } catch (Exception e) {
                    log.warn("Missing ZoneName: " + idAnnotation.value());
                }
            }
        }
    }

    public final void addZoneHandlerClass(ZoneName zoneName, Class<? extends ZoneHandler> handler) {
        handlers.put(zoneName, handler);
    }

    @Override
    public void load(CountDownLatch progressLatch) {
        log.info("Zone engine load started");
        scriptManager = new ScriptManager();

        AggregatedClassListener acl = new AggregatedClassListener();
        acl.addClassListener(new OnClassLoadUnloadListener());
        acl.addClassListener(new ScheduledTaskClassListener());
        acl.addClassListener(new ZoneHandlerClassListener());
        scriptManager.setGlobalClassListener(acl);

        try {
            scriptManager.load(ZONE_DESCRIPTOR_FILE);
            log.info("Loaded " + handlers.size() + " zone handlers.");
        } catch (IllegalStateException e) {
            log.warn("Can't initialize instance handlers.", e.getMessage());
        } catch (Exception e) {
            throw new GameServerError("Can't initialize instance handlers.", e);
        } finally {
            if (progressLatch != null) {
                progressLatch.countDown();
            }
        }
    }

    @Override
    public void shutdown() {
        log.info("Zone engine shutdown started");
        scriptManager.shutdown();
        scriptManager = null;
        handlers.clear();
        log.info("Zone engine shutdown complete");
    }

    /**
     * @param mapId
     * @return
     */
    public Map<ZoneName, ZoneInstance> getZoneInstancesByWorldId(int mapId) {

        Map<ZoneName, ZoneInstance> zones = new HashMap<>();
        int worldSize = DataManager.WORLD_MAPS_DATA.getTemplate(mapId).getWorldSize();
        WorldZoneTemplate zone = new WorldZoneTemplate(worldSize, mapId);
        PolyArea fullArea = new PolyArea(zone.getName(), mapId, zone.getPoints().getPoint(), zone.getPoints().getBottom(), zone.getPoints()
                .getTop());
        ZoneInstance fullMap = new ZoneInstance(mapId, new ZoneInfo(fullArea, zone));
        fullMap.addHandler(getNewZoneHandler(zone.getName()));
        zones.put(zone.getName(), fullMap);

        Collection<ZoneInfo> areas = this.zoneByMapIdMap.get(mapId);
        if (areas == null)
            return zones;
        ShieldService.getInstance().load(mapId);

        for (ZoneInfo area : areas) {
            ZoneInstance instance;
            switch (area.getZoneTemplate().getZoneType()) {
                case FLY:
                    instance = new FlyZoneInstance(mapId, area);
                    break;
                case FORT:
                    instance = new SiegeZoneInstance(mapId, area);
                    SiegeLocation siege = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations().get(area.getZoneTemplate().getSiegeId().get(0));
                    if (siege != null) {
                        siege.addZone((SiegeZoneInstance) instance);
                        if (GeoDataConfig.GEO_SHIELDS_ENABLE)
                            ShieldService.getInstance().attachShield(siege);
                    }
                    break;
                case ARTIFACT:
                    instance = new SiegeZoneInstance(mapId, area);
                    for (int artifactId : area.getZoneTemplate().getSiegeId()) {
                        SiegeLocation artifact = DataManager.SIEGE_LOCATION_DATA.getArtifacts().get(artifactId);
                        if (artifact == null) {
                            log.warn("Missing siege location data for zone " + area.getZoneTemplate().getName().name());
                        } else {
                            artifact.addZone((SiegeZoneInstance) instance);
                        }
                    }
                    break;
                case PVP:
                    instance = new PvPZoneInstance(mapId, area);
                    break;
                case NEUTRAL:
                    instance = new NeutralZoneInstance(mapId, area);
                    break;
                default:
                    instance = new ZoneInstance(mapId, area);
            }
            instance.addHandler(getNewZoneHandler(area.getZoneTemplate().getName()));
            zones.put(area.getZoneTemplate().getName(), instance);
        }
        return zones;
    }

    /**
     * Method for single instances of meshes (if specified in mesh_materials.xml)
     *
     * @param geometry
     * @param worldId
     * @param materialId
     */
    public void createMaterialZoneTemplate(AionMesh geometry, int worldId, int materialId, boolean failOnMissing) {
        ZoneName zoneName;
        if (failOnMissing)
            zoneName = ZoneName.get(geometry.toString() + "_" + worldId);
        else
            zoneName = ZoneName.createOrGet(geometry.toString() + "_" + worldId);

        if (zoneName.name().equals(ZoneName.NONE))
            return;

        ZoneHandler handler = collidableHandlers.get(zoneName);
        if (handler == null) {
            if (materialId == 11) {
                if (GeoDataConfig.GEO_SHIELDS_ENABLE) {
                    handler = new SiegeShield(geometry);
                    ShieldService.getInstance().registerShield(worldId, (SiegeShield) handler);
                } else
                    return;
            } else {
                MaterialTemplate template = DataManager.MATERIAL_DATA.getTemplate(materialId);
                if (template == null)
                    return;
                handler = new MaterialZoneHandler(geometry, template);
            }
            collidableHandlers.put(zoneName, handler);
        } else {
            log.warn("Duplicate material mesh: " + zoneName.toString());
        }

        Collection<ZoneInfo> areas = this.zoneByMapIdMap.get(worldId);
        if (areas == null) {
            this.zoneByMapIdMap.put(worldId, new ArrayList<ZoneInfo>());
            areas = this.zoneByMapIdMap.get(worldId);
        }
        ZoneInfo zoneInfo = null;
        for (ZoneInfo area : areas) {
            if (area.getZoneTemplate().getName().equals(zoneName)) {
                zoneInfo = area;
                break;
            }
        }
        if (zoneInfo == null) {
            MaterialZoneTemplate zoneTemplate = new MaterialZoneTemplate(geometry, worldId);
            // maybe add to zone data if needed search ?
            Area zoneInfoArea = null;
            if (zoneTemplate.getSphere() != null) {
                zoneInfoArea = new SphereArea(zoneName, worldId, zoneTemplate.getSphere().getX(), zoneTemplate.getSphere().getY(), zoneTemplate
                        .getSphere().getZ(), zoneTemplate.getSphere().getR());
            } else if (zoneTemplate.getCylinder() != null) {
                zoneInfoArea = new CylinderArea(zoneName, worldId, zoneTemplate.getCylinder().getX(), zoneTemplate.getCylinder().getY(),
                        zoneTemplate.getCylinder().getR(), zoneTemplate.getCylinder().getBottom(), zoneTemplate.getCylinder().getTop());
            } else if (zoneTemplate.getSemisphere() != null) {
                zoneInfoArea = new SemisphereArea(zoneName, worldId, zoneTemplate.getSemisphere().getX(), zoneTemplate.getSemisphere().getY(),
                        zoneTemplate.getSemisphere().getZ(), zoneTemplate.getSemisphere().getR());
            }
            if (zoneInfoArea != null) {
                zoneInfo = new ZoneInfo(zoneInfoArea, zoneTemplate);
                areas.add(zoneInfo);
            }
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final ZoneService instance = new ZoneService();
    }
}
