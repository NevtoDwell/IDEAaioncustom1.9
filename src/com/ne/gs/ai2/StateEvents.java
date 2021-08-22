/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

import java.util.Arrays;
import java.util.EnumSet;

import com.ne.gs.ai2.event.AIEventType;

/**
 * @author ATracer
 */
public enum StateEvents {
    CREATED_EVENTS(AIEventType.SPAWNED),
    DESPAWN_EVENTS(AIEventType.RESPAWNED, AIEventType.SPAWNED),
    DEAD_EVENTS(AIEventType.DESPAWNED,
        AIEventType.DROP_REGISTERED);

    private final EnumSet<AIEventType> events;

    private StateEvents(AIEventType... aiEventTypes) {
        events = EnumSet.copyOf(Arrays.asList(aiEventTypes));
    }

    public boolean hasEvent(AIEventType event) {
        return events.contains(event);
    }

}
