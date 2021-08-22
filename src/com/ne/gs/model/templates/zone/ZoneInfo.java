/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.zone;

import com.ne.gs.model.geometry.Area;


/**
 * @author MrPoke
 */
public class ZoneInfo {

    private final Area area;
    private final ZoneTemplate zoneTemplate;

    /**
     * @param area
     * @param zoneTemplate
     */
    public ZoneInfo(Area area, ZoneTemplate zoneTemplate) {
        this.area = area;
        this.zoneTemplate = zoneTemplate;
    }


    /**
     * @return the area
     */
    public Area getArea() {
        return area;
    }


    /**
     * @return the zoneTemplate
     */
    public ZoneTemplate getZoneTemplate() {
        return zoneTemplate;
    }
}
