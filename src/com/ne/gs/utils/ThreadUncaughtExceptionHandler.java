/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 */
public class ThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(ThreadUncaughtExceptionHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Critical Error - Thread: " + t.getName() + " terminated abnormaly: " + e, e);
        if (e instanceof OutOfMemoryError) {
            // TODO try get some memory or restart
            log.error("Out of memory! You should get more memory!");
        }
    }
}
