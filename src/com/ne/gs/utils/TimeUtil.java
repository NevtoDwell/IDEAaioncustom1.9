/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

/**
 * @author ATracer
 */
public final class TimeUtil {

    /**
     * Check whether supplied time in ms is expired
     */
    public static boolean isExpired(long time) {
        return time < System.currentTimeMillis();
    }
}
