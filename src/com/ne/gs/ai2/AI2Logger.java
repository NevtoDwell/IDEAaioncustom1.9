/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.AIConfig;
import com.ne.gs.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public final class AI2Logger {

    private static final Logger log = LoggerFactory.getLogger(AI2Logger.class);

    public static void info(AbstractAI ai, String message) {
        if (ai.isLogging()) {
            log.info("[AI2] " + ai.getOwner().getObjectId() + " - " + message);
        }
    }

    public static void info(AI2 ai, String message) {
        info((AbstractAI) ai, message);
    }

    /**
     * @param owner
     * @param message
     */
    public static void moveinfo(Creature owner, String message) {
        if (AIConfig.MOVE_DEBUG && owner.getAi2().isLogging()) {
            log.info("[AI2] " + owner.getObjectId() + " - " + message);
        }
    }
}
