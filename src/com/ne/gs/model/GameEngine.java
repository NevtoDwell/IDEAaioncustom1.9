/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

import java.util.concurrent.CountDownLatch;

/**
 * @author ATracer
 */
public interface GameEngine {

    /**
     * Load resources for engine
     */
    void load(CountDownLatch progressLatch);

    /**
     * Cleanup resources for engine
     */
    void shutdown();
}
