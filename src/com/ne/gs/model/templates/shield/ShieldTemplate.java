/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.shield;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.utils3d.Point3D;

/**
 * @author M@xx, Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Shield")
public class ShieldTemplate {

    @XmlAttribute(name = "name")
    protected String name;

    @XmlAttribute(name = "map")
    protected int map;

    @XmlAttribute(name = "id")
    protected int id;

    @XmlAttribute(name = "radius")
    protected float radius;

    @XmlElement(name = "center")
    protected ShieldPoint center;

    public String getName() {
        return name;
    }

    public int getMap() {
        return map;
    }

    public float getRadius() {
        return radius;
    }

    public ShieldPoint getCenter() {
        return center;
    }

    public int getId() {
        return id;
    }

    public ShieldTemplate() {
    }

    ;

    public ShieldTemplate(String name, int mapId, Point3D center) {
        this.name = name;
        this.map = mapId;
        this.radius = 6;
        this.center = new ShieldPoint(center);
    }
}
