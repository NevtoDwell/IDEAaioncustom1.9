/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.templates.event.EventTemplate;
import com.ne.gs.spawnengine.SpawnHandlerType;

/**
 * @author xTz
 * @modified Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Spawn")
public class Spawn {

    @XmlAttribute(name = "custom")
    private Boolean isCustom = false;

    @XmlAttribute(name = "handler")
    private SpawnHandlerType handler;

    @XmlAttribute(name = "pool")
    private Integer pool = 0;

    @XmlAttribute(name = "respawn_time")
    private Integer respawnTime = 0;

    @XmlAttribute(name = "npc_id", required = true)
    private int npcId;

    @XmlAttribute(name = "difficult_id")
    private int difficultId;

    @XmlElement(name = "temporary_spawn")
    private TemporarySpawn temporaySpawn;
    @XmlElement(name = "spot")
    private List<SpawnSpotTemplate> spawnTemplates;

    @XmlTransient
    private EventTemplate eventTemplate;

    public Spawn() {
    }

    public Spawn(int npcId, int respawnTime, SpawnHandlerType handler) {
        this.npcId = npcId;
        this.respawnTime = respawnTime;
        this.handler = handler;
    }

    void beforeMarshal(Marshaller marshaller) {
        if (pool == 0) {
            pool = null;
        }
        if (isCustom == false) {
            isCustom = null;
        }
    }

    void afterMarshal(Marshaller marshaller) {
        if (isCustom == null) {
            isCustom = false;
        }
        if (pool == null) {
            pool = 0;
        }
    }

    public int getNpcId() {
        return npcId;
    }

    public int getPool() {
        return pool;
    }

    public TemporarySpawn getTemporarySpawn() {
        return temporaySpawn;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public SpawnHandlerType getSpawnHandlerType() {
        return handler;
    }

    public List<SpawnSpotTemplate> getSpawnSpotTemplates() {
        if (spawnTemplates == null) {
            spawnTemplates = new ArrayList<>();
        }
        return spawnTemplates;
    }

    public void addSpawnSpot(SpawnSpotTemplate template) {
        getSpawnSpotTemplates().add(template);
    }

    public boolean isCustom() {
        return isCustom == null ? false : isCustom;
    }

    public void setCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public boolean isEventSpawn() {
        return eventTemplate != null;
    }

    public EventTemplate getEventTemplate() {
        return eventTemplate;
    }

    public void setEventTemplate(EventTemplate eventTemplate) {
        this.eventTemplate = eventTemplate;
    }

    public int getDifficultId() {
        return difficultId;
    }
}
