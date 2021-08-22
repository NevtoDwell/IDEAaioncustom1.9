/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ne.gs.model.geometry.*;
import com.ne.gs.model.templates.zone.ZoneClassName;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.zone.ZoneInfo;
import com.ne.gs.model.templates.zone.ZoneTemplate;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "zones")
public class ZoneData {

    @XmlElement(name = "zone")
    protected List<ZoneTemplate> zoneList;

    @XmlTransient
    private final TIntObjectHashMap<List<ZoneInfo>> zoneNameMap = new TIntObjectHashMap<>();

    @XmlTransient
    private HashMap<ZoneTemplate, Integer> weatherZoneIds = new HashMap<ZoneTemplate, Integer>();

    @XmlTransient
    private int count;

    protected void afterUnmarshal(Unmarshaller u, Object parent) {
        int lastMapId = 0;
        int weatherZoneId = 1;
        for (ZoneTemplate zone : zoneList) {
            Area area = null;
            switch (zone.getAreaType()) {
                case POLYGON:
                    area = new PolyArea(zone.getName(), zone.getMapid(), zone.getPoints().getPoint(), zone.getPoints().getBottom(), zone.getPoints()
                            .getTop());
                    break;
                case CYLINDER:
                    area = new CylinderArea(zone.getName(), zone.getMapid(), zone.getCylinder().getX(), zone.getCylinder().getY(), zone.getCylinder()
                            .getR(), zone.getCylinder().getBottom(), zone.getCylinder().getTop());
                    break;
                case SPHERE:
                    area = new SphereArea(zone.getName(), zone.getMapid(), zone.getSphere().getX(), zone.getSphere().getY(), zone.getSphere().getZ(),
                            zone.getSphere().getR());
                    break;
                case SEMISPHERE:
                    area = new SemisphereArea(zone.getName(), zone.getMapid(), zone.getSemisphere().getX(), zone.getSemisphere().getY(), zone.getSemisphere().getZ(),
                            zone.getSemisphere().getR());
            }
            if (area != null) {
                List<ZoneInfo> zones = zoneNameMap.get(zone.getMapid());
                if (zones == null) {
                    zones = new ArrayList<ZoneInfo>();
                    zoneNameMap.put(zone.getMapid(), zones);
                }
                if (zone.getZoneType() == ZoneClassName.WEATHER) {
                    if (lastMapId != zone.getMapid()) {
                        lastMapId = zone.getMapid();
                        weatherZoneId = 1;
                    }
                    weatherZoneIds.put(zone, weatherZoneId++);
                }
                zones.add(new ZoneInfo(area, zone));
                count++;
            }
        }
        zoneList.clear();
        zoneList = null;
    }

    public TIntObjectHashMap<List<ZoneInfo>> getZones() {
        return zoneNameMap;
    }

    public int size() {
        return zoneNameMap.size();
    }

    /**
     * Weather zone ID it's an order number (starts from 1)
     */
    public int getWeatherZoneId(ZoneTemplate template) {
        Integer id = weatherZoneIds.get(template);
        if (id == null)
            return 0;
        return id;
    }

}
