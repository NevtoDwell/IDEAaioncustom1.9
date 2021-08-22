/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.event;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.SpawnsData2;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.Guides.GuideTemplate;
import com.ne.gs.model.templates.spawns.Spawn;
import com.ne.gs.model.templates.spawns.SpawnMap;
import com.ne.gs.model.templates.spawns.SpawnSpotTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.gametime.DateTimeUtil;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author Rolandas
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventTemplate", propOrder = {"quests", "spawns", "inventoryDrop", "surveys"})
public class EventTemplate {

    private static final Logger log = LoggerFactory.getLogger(EventTemplate.class);

    @XmlElement(name = "quests", required = false)
    protected EventQuestList quests;

    @XmlElement(name = "spawns", required = false)
    protected SpawnsData2 spawns;

    @XmlElement(name = "inventory_drop", required = false)
    protected InventoryDrop inventoryDrop;

    @XmlList
    @XmlElement(name = "surveys", required = false)
    protected List<String> surveys;

    @XmlAttribute(name = "name", required = true)
    protected String name;

    @XmlAttribute(name = "start", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;

    @XmlAttribute(name = "end", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDate;

    @XmlAttribute(name = "theme", required = false)
    private String theme;

    @XmlTransient
    protected List<VisibleObject> spawnedObjects;

    @XmlTransient
    private Future<?> invDropTask = null;

    @XmlTransient
    volatile boolean isStarted = false;

    public String getName() {
        return name;
    }

    public DateTime getStartDate() {
        return DateTimeUtil.getDateTime(startDate.toGregorianCalendar());
    }

    public DateTime getEndDate() {
        return DateTimeUtil.getDateTime(endDate.toGregorianCalendar());
    }

    public List<Integer> getStartableQuests() {
        if (quests == null) {
            return new ArrayList<>();
        }
        return quests.getStartableQuests();
    }

    public List<Integer> getMaintainableQuests() {
        if (quests == null) {
            return new ArrayList<>();
        }
        return quests.getMaintainQuests();
    }

    public boolean isActive() {
        return getStartDate().isBeforeNow() && getEndDate().isAfterNow();
    }

    public boolean isExpired() {
        return !isActive();
    }

    public void setStarted() {
        isStarted = true;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void Start() {
        if (isStarted) {
            return;
        }

        if (spawns != null && spawns.size() > 0) {
            if (spawnedObjects == null) {
                spawnedObjects = new ArrayList<>();
            }
            for (SpawnMap map : spawns.getTemplates()) {
                DataManager.SPAWNS_DATA2.addNewSpawnMap(map);
                Collection<Integer> instanceIds = World.getInstance().getWorldMap(map.getMapId()).getAvailableInstanceIds();
                for (Integer instanceId : instanceIds) {
                    int spawnCount = 0;
                    for (Spawn spawn : map.getSpawns()) {
                        spawn.setEventTemplate(this);
                        if (spawn.getPool() > spawn.getSpawnSpotTemplates().size()) {
                            Collections.shuffle(spawn.getSpawnSpotTemplates());
                        }
                        int pool = 0;
                        for (SpawnSpotTemplate spot : spawn.getSpawnSpotTemplates()) {
                            if ((++pool) > spawn.getPool()) {
                                break;
                            }
                            SpawnTemplate t = SpawnEngine.addNewSpawn(map.getMapId(), spawn.getNpcId(), spot.getX(), spot.getY(),
                                spot.getZ(), spot.getHeading(), spawn.getRespawnTime());
                            t.setEventTemplate(this);
                            SpawnEngine.spawnObject(t, instanceId);
                            spawnCount++;
                        }
                    }
                    log.info("Spawned event objects in " + map.getMapId() + " [" + instanceId + "] : " + spawnCount + " ("
                        + this.getName() + ")");
                }
            }
            DataManager.SPAWNS_DATA2.afterUnmarshal(null, null);
            DataManager.SPAWNS_DATA2.clearTemplates();
        }

        if (inventoryDrop != null) {
            invDropTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                        @Override
                        public void visit(Player player) {
                            if (player.getCommonData().getLevel() >= inventoryDrop.getStartLevel()) {
                                ItemService.dropItemToInventory(player, inventoryDrop.getDropItem());
                            }
                        }
                    });
                }
            }, inventoryDrop.getInterval() * 60000, inventoryDrop.getInterval() * 60000);
        }

        if (surveys != null) {
            for (String survey : surveys) {
                GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(survey);
                if (template != null) {
                    template.setActivated(true);
                }
            }
        }

        isStarted = true;
    }

    public void Stop() {
        if (!isStarted) {
            return;
        }

        if (spawnedObjects != null) {
            for (VisibleObject o : spawnedObjects) {
                if (o.isSpawned()) {
                    o.getController().delete();
                }
            }
            DataManager.SPAWNS_DATA2.removeEventSpawnObjects(spawnedObjects);
            log.info("Despawned " + spawnedObjects.size() + " event objects (" + this.getName() + ")");
            spawnedObjects.clear();
            spawnedObjects = null;
        }

        if (invDropTask != null) {
            invDropTask.cancel(false);
            invDropTask = null;
        }

        if (surveys != null) {
            for (String survey : surveys) {
                GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(survey);
                if (template != null) {
                    template.setActivated(false);
                }
            }
        }

        isStarted = false;
    }

    public void addSpawnedObject(VisibleObject object) {
        if (spawnedObjects == null) {
            spawnedObjects = new ArrayList<>();
        }
        spawnedObjects.add(object);
    }

    /**
     * @return the theme name
     */
    public String getTheme() {
        if (theme != null) {
            return theme.toLowerCase();
        }
        return theme;
    }

}
