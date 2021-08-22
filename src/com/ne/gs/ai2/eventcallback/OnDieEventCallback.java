/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.eventcallback;

import com.ne.gs.ai2.AbstractAI;
import com.ne.gs.ai2.event.AIEventType;

/**
 * Callback for {@link AIEventType#DIED} event
 *
 * @author SoulKeeper
 */
public abstract class OnDieEventCallback extends OnHandleAIGeneralEvent {
    @Override
    protected void onAIHandleGeneralEvent(AbstractAI ai, AIEventType eventType) {
        if (AIEventType.DIED == eventType) {
            onDie(ai);
        }
    }

    public abstract void onDie(AbstractAI ai);
}
