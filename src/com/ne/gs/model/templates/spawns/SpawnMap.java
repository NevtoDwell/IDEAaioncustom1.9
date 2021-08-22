/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.templates.spawns.siegespawns.SiegeSpawn;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SpawnMap")
public class SpawnMap {

    @XmlElement(name = "spawn")
    private List<Spawn> spawns;

    @XmlElement(name = "siege_spawn")
    private List<SiegeSpawn> siegeSpawns;

    @XmlAttribute(name = "map_id")
    private int mapId;

    public SpawnMap() {
    }

    public SpawnMap(int mapId) {
        this.mapId = mapId;
    }

    public int getMapId() {
        return mapId;
    }

    public List<Spawn> getSpawns() {
        if (spawns == null) {
            spawns = new ArrayList<>();
        }
        return spawns;
    }

    public void addSpawns(Spawn spawns) {
        getSpawns().add(spawns);
    }

    public void removeSpawns(Spawn spawns) {
        getSpawns().remove(spawns);
    }

    public List<SiegeSpawn> getSiegeSpawns() {
        if (siegeSpawns == null) {
            siegeSpawns = new ArrayList<>();
        }
        return siegeSpawns;
    }

    public void addSiegeSpawns(SiegeSpawn spawns) {
        getSiegeSpawns().add(spawns);
    }
}
