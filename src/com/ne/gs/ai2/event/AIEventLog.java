/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.event;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author ATracer
 */
public class AIEventLog extends LinkedBlockingDeque<AIEventType> {

    private static final long serialVersionUID = -7234174243343636729L;

    public AIEventLog() {
        super();
    }

    /**
     * @param capacity
     */
    public AIEventLog(int capacity) {
        super(capacity);
    }

    @Override
    public synchronized boolean offerFirst(AIEventType e) {
        if (remainingCapacity() == 0) {
            removeLast();
        }
        super.offerFirst(e);
        return true;
    }
}
