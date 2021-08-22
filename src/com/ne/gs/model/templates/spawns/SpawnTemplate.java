/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.event.EventTemplate;
import com.ne.gs.spawnengine.SpawnHandlerType;

/**
 * @author xTz
 * @modified Rolandas
 */
public class SpawnTemplate {

    private float x;
    private float y;
    private float z;
    private final int h;
    private int staticId;
    private final int randomWalk;
    private String walkerId;
    private int walkerIdx;
    private final int fly;
    private String anchor;
    private boolean isUsed;
    private final SpawnGroup2 spawnGroup;
    private EventTemplate eventTemplate;
    private SpawnModel model;
    private int state;
    private int creatorId;
    private String masterName = "";
    private TemporarySpawn temporarySpawn;
    private VisibleObject visibleObject;

    public SpawnTemplate(SpawnGroup2 spawnGroup, SpawnSpotTemplate spot) {
        this.spawnGroup = spawnGroup;
        x = spot.getX();
        y = spot.getY();
        z = spot.getZ();
        h = spot.getHeading();
        staticId = spot.getStaticId();
        randomWalk = spot.getRandomWalk();
        walkerId = spot.getWalkerId();
        fly = spot.getFly();
        anchor = spot.getAnchor();
        walkerIdx = spot.getWalkerIndex();
        model = spot.getModel();
        state = spot.getState();
        temporarySpawn = spot.getTemporarySpawn();
    }

    public SpawnTemplate(SpawnGroup2 spawnGroup, float x, float y, float z, int heading, int randWalk, String walkerId,
                         int staticId, int fly) {
        this.spawnGroup = spawnGroup;
        this.x = x;
        this.y = y;
        this.z = z;
        h = heading;
        this.randomWalk = randWalk;
        this.walkerId = walkerId;
        this.staticId = staticId;
        this.fly = fly;
        addTemplate();
    }

    private void addTemplate() {
        spawnGroup.addSpawnTemplate(this);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getHeading() {
        return h;
    }

    public int getStaticId() {
        return staticId;
    }

    public void setStaticId(int staticId) {
        this.staticId = staticId;
    }

    public int getRandomWalk() {
        return randomWalk;
    }

    public int getFly() {
        return fly;
    }

    public boolean canFly() {
        return fly > 0;
    }

    public void setUse(boolean use) {
        isUsed = use;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public int getNpcId() {
        return spawnGroup.getNpcId();
    }

    public int getWorldId() {
        return spawnGroup.getWorldId();
    }

    public SpawnTemplate changeTemplate() {
        return spawnGroup.getRndTemplate();
    }

    public int getRespawnTime() {
        return spawnGroup.getRespawnTime();
    }

    public void setRespawnTime(int respawnTime) {
        spawnGroup.setRespawnTime(respawnTime);
    }

    public TemporarySpawn getTemporarySpawn() {
        return temporarySpawn != null ? temporarySpawn : spawnGroup.geTemporarySpawn();
    }

    public SpawnHandlerType getHandlerType() {
        return spawnGroup.getHandlerType();
    }

    public String getAnchor() {
        return anchor;
    }

    public boolean hasRandomWalk() {
        return randomWalk != 0;
    }

    public boolean isNoRespawn() {
        return spawnGroup.getRespawnTime() == 0;
    }

    public boolean hasPool() {
        return spawnGroup.hasPool();
    }

    public String getWalkerId() {
        return walkerId;
    }

    public void setWalkerId(String walkerId) {
        this.walkerId = walkerId;
    }

    public int getWalkerIndex() {
        return walkerIdx;
    }

    public boolean isTemporarySpawn() {
        return spawnGroup.isTemporarySpawn();
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

    public SpawnModel getModel() {
        return model;
    }

    public int getState() {
        return state;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public VisibleObject getVisibleObject() {
        return visibleObject;
    }

    public void setVisibleObject(VisibleObject visibleObject) {
        this.visibleObject = visibleObject;
    }
}
