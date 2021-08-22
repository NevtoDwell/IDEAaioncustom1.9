/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.curingzones;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CuringTemplate")
public class CuringTemplate {

    @XmlAttribute(name = "map_id")
    protected int mapId;

    @XmlAttribute(name = "x")
    protected float x;

    @XmlAttribute(name = "y")
    protected float y;

    @XmlAttribute(name = "z")
    protected float z;

    @XmlAttribute(name = "range")
    protected float range;

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int value) {
        mapId = value;
    }

    public float getX() {
        return x;
    }

    public void setX(float value) {
        x = value;
    }

    public float getY() {
        return y;
    }

    public void setY(float value) {
        y = value;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float value) {
        z = value;
    }

    public float getRange() {
        return range;
    }
}
