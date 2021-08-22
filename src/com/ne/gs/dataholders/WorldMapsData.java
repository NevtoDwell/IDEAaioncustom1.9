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
import java.util.Iterator;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.world.WorldMapTemplate;

/**
 * Object of this class is containing <tt>WorldMapTemplate</tt> objects for all world maps. World maps are defined in
 * data/static_data/world_maps.xml file.
 *
 * @author Luno
 */
@XmlRootElement(name = "world_maps")
@XmlAccessorType(XmlAccessType.NONE)
public class WorldMapsData implements Iterable<WorldMapTemplate> {

    @XmlElement(name = "map")
    protected List<WorldMapTemplate> worldMaps;

    protected TIntObjectHashMap<WorldMapTemplate> worldIdMap = new TIntObjectHashMap<>();

    protected void afterUnmarshal(Unmarshaller u, Object parent) {
        for (WorldMapTemplate map : worldMaps) {
            worldIdMap.put(map.getMapId(), map);
        }

        worldMaps = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<WorldMapTemplate> iterator() {
        return worldIdMap.valueCollection().iterator();
    }

    /**
     * Returns the count of maps.
     *
     * @return worldMaps.size()
     */
    public int size() {
        return worldIdMap.size();
    }

    /**
     * @param worldId
     *
     * @return
     */
    public WorldMapTemplate getTemplate(int worldId) {
        return worldIdMap.get(worldId);
    }
}
