/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.flypath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author KID
 */
@XmlRootElement(name = "flypath_location")
@XmlAccessorType(XmlAccessType.NONE)
public class FlyPathEntry {

    @XmlAttribute(name = "id", required = true)
    private short id;
    @XmlAttribute(name = "sx", required = true)
    private float startX;
    @XmlAttribute(name = "sy", required = true)
    private float startY;
    @XmlAttribute(name = "sz", required = true)
    private float startZ;
    @XmlAttribute(name = "sworld", required = true)
    private int sworld;

    @XmlAttribute(name = "ex", required = true)
    private float endX;
    @XmlAttribute(name = "ey", required = true)
    private float endY;
    @XmlAttribute(name = "ez", required = true)
    private float endZ;
    @XmlAttribute(name = "eworld", required = true)
    private int eworld;

    @XmlAttribute(name = "time", required = true)
    private float time;

    public short getId() {
        return id;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getStartZ() {
        return startZ;
    }

    public float getEndX() {
        return endX;
    }

    public float getEndY() {
        return endY;
    }

    public float getEndZ() {
        return endZ;
    }

    public int getStartWorldId() {
        return sworld;
    }

    public int getEndWorldId() {
        return eworld;
    }

    public int getTimeInMs() {
        return (int) (time * 1000);
    }
}
