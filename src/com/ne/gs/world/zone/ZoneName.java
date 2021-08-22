/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone;

import java.util.Map;
import gnu.trove.map.hash.THashMap;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZoneName {

    private final static Logger log = LoggerFactory.getLogger(ZoneName.class);

    private static final FastMap<String, ZoneName> zoneNames = new FastMap<String, ZoneName>();
    public static final String NONE = "NONE";
    public static final String ABYSS_CASTLE = "_ABYSS_CASTLE_AREA_";

    static {
        zoneNames.put(NONE, new ZoneName(NONE));
        zoneNames.put(ABYSS_CASTLE, new ZoneName(ABYSS_CASTLE));
    }

    private String _name;

    private ZoneName(String name) {
        this._name = name;
    }

    public String name() {
        return _name;
    }

    public int id() {
        return _name.hashCode();
    }

    public static final ZoneName createOrGet(String name) {
        name = name.toUpperCase();
        if (zoneNames.containsKey(name))
            return zoneNames.get(name);
        ZoneName newZone = new ZoneName(name);
        zoneNames.put(name, newZone);
        return newZone;
    }

    public static final int getId(String name) {
        name = name.toUpperCase();
        if (zoneNames.containsKey(name))
            return zoneNames.get(name).id();
        return zoneNames.get(NONE).id();
    }

    public static final ZoneName get(String name) {
        name = name.toUpperCase();
        if (zoneNames.containsKey(name))
            return zoneNames.get(name);
        log.warn("Missing zone : " + name);
        return zoneNames.get(NONE);
    }

    @Override
    public String toString() {
        return _name;
    }
}
