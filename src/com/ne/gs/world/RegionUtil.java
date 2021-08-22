/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world;

import com.ne.gs.configs.main.WorldConfig;

/**
 * @author ATracer
 */
public final class RegionUtil {

    public static final int X_3D_OFFSET = 1000000;
    public static final int Y_3D_OFFSET = 1000;
    public static final int X_2D_OFFSET = 1000;

    /**
     * @param regionSize
     * @param x
     * @param y
     *
     * @return
     */
    public static int get2DRegionId(int regionSize, float x, float y) {
        return (int) x / regionSize * X_2D_OFFSET + (int) y / regionSize;
    }

    /**
     * @param regionSize
     * @param x
     * @param y
     * @param z
     *
     * @return
     */
    public static int get3DRegionId(int regionSize, float x, float y, float z) {
        return (int) x / regionSize * X_3D_OFFSET + (int) y / regionSize * Y_3D_OFFSET + (int) z / regionSize;
    }

    /**
     * @param x
     * @param y
     *
     * @return
     */
    public static int get2dRegionId(float x, float y) {
        return get2DRegionId(WorldConfig.WORLD_REGION_SIZE, x, y);
    }

    /**
     * @param x
     * @param y
     * @param z
     *
     * @return
     */
    public static int get3dRegionId(float x, float y, float z) {
        return get3DRegionId(WorldConfig.WORLD_REGION_SIZE, x, y, z);
    }

    /**
     * @param regionId
     *
     * @return
     */
    public static int getXFrom2dRegionId(int regionId) {
        return regionId / X_2D_OFFSET * WorldConfig.WORLD_REGION_SIZE;
    }

    /**
     * @param regionId
     *
     * @return
     */
    public static int getYFrom2dRegionId(int regionId) {
        return regionId % X_2D_OFFSET * WorldConfig.WORLD_REGION_SIZE;
    }

    /**
     * @param regionId
     *
     * @return
     */
    public static int getXFrom3dRegionId(int regionId) {
        return regionId / X_3D_OFFSET * WorldConfig.WORLD_REGION_SIZE;
    }

    /**
     * @param regionId
     *
     * @return
     */
    public static int getYFrom3dRegionId(int regionId) {
        return regionId % X_3D_OFFSET / Y_3D_OFFSET * WorldConfig.WORLD_REGION_SIZE;
    }

    /**
     * @param regionId
     *
     * @return
     */
    public static int getZFrom3dRegionId(int regionId) {
        return regionId % X_3D_OFFSET % Y_3D_OFFSET * WorldConfig.WORLD_REGION_SIZE;
    }
}