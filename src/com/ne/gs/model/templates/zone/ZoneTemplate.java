/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.zone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.world.zone.ZoneName;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Zone")
public class ZoneTemplate {

    @XmlElement
    protected Points points;
    @XmlElement
    protected Cylinder cylinder;
    @XmlElement
    protected Sphere sphere;
    @XmlAttribute
    protected int flags = -1;
    @XmlAttribute
    protected int priority;
    @XmlTransient
    private String name;
    @XmlTransient
    private ZoneName zoneName;
    @XmlAttribute
    protected int mapid;
    @XmlAttribute(name = "siege_id")
    protected List<Integer> siegeId;
    @XmlAttribute(name = "area_type")
    protected AreaType areaType = AreaType.POLYGON;
    @XmlAttribute(name = "zone_type")
    protected ZoneClassName zoneType = ZoneClassName.SUB;

    @XmlElement
    protected Semisphere semisphere;

    @XmlAttribute(name = "name")
    public String getXmlName() {
        return name;
    }

    protected void setXmlName(String name) {
        zoneName = ZoneName.createOrGet(name);
        this.name = zoneName.name();
    }

    /**
     * Gets the value of the points property.
     */
    public Points getPoints() {
        return points;
    }

    public Cylinder getCylinder() {
        return cylinder;
    }

    public Sphere getSphere() {
        return sphere;
    }

    public Semisphere getSemisphere() {
        return semisphere;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets the value of the name property.
     */
    public ZoneName getName() {
        return zoneName;
    }

    /**
     * Gets the value of the mapid property.
     */
    public int getMapid() {
        return mapid;
    }

    /**
     * @return the type
     */
    public AreaType getAreaType() {
        return areaType;
    }

    /**
     * @return the zoneType
     */
    public ZoneClassName getZoneType() {
        return zoneType;
    }

    public List<Integer> getSiegeId() {
        return siegeId;
    }

    public int getFlags() {
        return flags;
    }
}
