/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.main;

import com.ne.commons.configuration.Property;

/**
 * @author lord_rex
 */
public final class ThreadConfig {

    /**
     * Thread basepoolsize
     */
    @Property(key = "gameserver.thread.basepoolsize", defaultValue = "2")
    public static int BASE_THREAD_POOL_SIZE;
    /**
     * Thread threadpercore
     */
    @Property(key = "gameserver.thread.threadpercore", defaultValue = "4")
    public static int EXTRA_THREAD_PER_CORE;
    /**
     * Thread runtime
     */
    @Property(key = "gameserver.thread.runtime", defaultValue = "5000")
    public static long MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING;
    public static int THREAD_POOL_SIZE;

    public static void load() {
        int baseThreadPoolSize = BASE_THREAD_POOL_SIZE;
        int extraThreadPerCore = EXTRA_THREAD_PER_CORE;

        THREAD_POOL_SIZE = baseThreadPoolSize + Runtime.getRuntime().availableProcessors() * extraThreadPerCore;
    }
}
