/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world;

import mw.engines.geo.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.annotations.Nullable;
import com.ne.commons.math.AionChannelPos;

/**
 * Position of object in the world.
 *
 * @author -Nemesiss-
 */
public class WorldPosition implements AionChannelPos {

    private static final Logger log = LoggerFactory.getLogger(WorldPosition.class);

    /**
     * Map id.
     */
    private int _mapId;

    /**
     * Map Region.
     */
    private MapRegion _mapRegion;

    /**
     * World position x, y, z
     */
    private Vector3f _pos = new Vector3f();

    /**
     * Value from 0 to 120 (120==0 actually)
     */
    private int _h;

    /**
     * indicating if object is spawned or not.
     */
    private boolean _spawned = false;

    public int getMapId() {
        return _mapId;
    }

    public void setMapId(int mapId) {
        _mapId = mapId;
    }

    public Vector3f getPoint() {
        return _pos.clone();
    }

    public float getX() {
        return _pos.x;
    }

    public void setX(float x) {
        _pos.x = x;
    }

    public void setH(int h) {
        _h = h;
    }

    public float getY() {
        return _pos.y;
    }

    public void setY(float y) {
        _pos.y = y;
    }

    public float getZ() {
        return _pos.z;
    }

    public void setZ(float z) {
        _pos.z = z;
    }

    public int getH() {
        return _h;
    }

    public float getDistance(Vector3f target){
        return target.distance(_pos);
    }

    public void setXYZH(float x, float y, float z, int h) {
        _pos.x = x;
        _pos.y = y;
        _pos.z = z;
        _h = h;
    }

    public MapRegion getMapRegion() {
        return _spawned ? _mapRegion : null;
    }

    public int getChannelId() {
        return _mapRegion.getParent().getInstanceId();
    }

    public int getInstanceId() {
        return getChannelId();
    }

    public int getInstanceCount() {
        return _mapRegion.getParent().getParent().getInstanceCount();
    }

    public boolean isInstanceMap() {
        return _mapRegion.getParent().getParent().isInstanceType();
    }

    public boolean isMapRegionActive() {
        return _mapRegion.isMapRegionActive();
    }


    /**
     * Returns the {@link World} instance in which this position is located. :D
     *
     * @return World
     */
    public World getWorld() {
        return _mapRegion.getWorld();
    }

    @Nullable
    public WorldMapInstance getWorldMapInstance() {
        MapRegion r = _mapRegion;
        if (r != null) {
            return r.getParent();
        }

        return null;
    }

    public boolean isSpawned() {
        return _spawned;
    }

    void setIsSpawned(boolean val) {
        _spawned = val;
    }

    void setMapRegion(MapRegion r) {
        _mapRegion = r;
    }

    @Override
    public String toString() {
        return String.format("WorldPosition [x=%s, y=%s, z=%s h=%d, spawned=%s, mapRegion=%s]", _pos.x, _pos.y, _pos.z, _h, _spawned, _mapRegion);
    }
}
