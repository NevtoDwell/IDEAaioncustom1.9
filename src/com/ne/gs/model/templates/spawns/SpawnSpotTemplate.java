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

/**
 * @author xTz
 * @modified Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpawnSpotTemplate")
public class SpawnSpotTemplate {

    @XmlAttribute(name = "state")
    private int state = 0;

    @XmlAttribute(name = "anchor")
    private String anchor;

    @XmlAttribute(name = "fly")
    private int fly = 0;

    @XmlAttribute(name = "walker_index")
    private int walkerIdx;

    @XmlAttribute(name = "walker_id")
    private String walkerId;

    @XmlAttribute(name = "random_walk")
    private int randomWalk = 0;

    @XmlAttribute(name = "static_id")
    private int staticId = 0;

    @XmlAttribute(name = "h", required = true)
    private int h;

    @XmlAttribute(name = "z", required = true)
    private float z;

    @XmlAttribute(name = "y", required = true)
    private float y;

    @XmlAttribute(name = "x", required = true)
    private float x;

    @XmlElement(name = "temporary_spawn")
    private TemporarySpawn temporaySpawn;

    @XmlElement(name = "model")
    private SpawnModel model;

    public SpawnSpotTemplate() {
    }

    public SpawnSpotTemplate(float x, float y, float z, int h, int randomWalk, String walkerId, Integer walkerIndex) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.h = h;
        if (randomWalk > 0) {
            this.randomWalk = randomWalk;
        }
        this.walkerId = walkerId;
        this.walkerIdx = walkerIndex;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
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

    public String getWalkerId() {
        return walkerId;
    }

    public void setWalkerId(String walkerId) {
        this.walkerId = walkerId;
    }

    public int getWalkerIndex() {
        return walkerIdx;
    }

    public int getRandomWalk() {
        return randomWalk;
    }

    public int getFly() {
        return fly;
    }

    public String getAnchor() {
        return anchor;
    }

    public SpawnModel getModel() {
        return model;
    }

    public int getState() {
        return state;
    }

    public TemporarySpawn getTemporarySpawn() {
        return temporaySpawn;
    }
}
