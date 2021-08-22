/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.eventcallback;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.EventNotifier;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.ai2.AbstractAI;
import com.ne.gs.ai2.event.AIEventType;

/**
 * Callback that is broadcasted when general ai event occurs.
 *
 * @author hex1r0
 */
public abstract class OnHandleAIGeneralEvent implements TypedCallback<Tuple2<AbstractAI, AIEventType>, Object> {
    // redirector to object notifier
    static {
        EventNotifier.GLOBAL.attach(new OnHandleAIGeneralEvent() {
            @Override
            protected void onAIHandleGeneralEvent(AbstractAI ai, AIEventType eventType) {
                ai.getOwner().getNotifier().fire(OnHandleAIGeneralEvent.class, Tuple2.of(ai, eventType));
            }
        });
    }

    @Override
    public final Object onEvent(@NotNull Tuple2<AbstractAI, AIEventType> e) {
        onAIHandleGeneralEvent(e._1, e._2);
        return null;
    }

    @NotNull
    @Override
    public final String getType() {
        return OnHandleAIGeneralEvent.class.getName();
    }

    protected abstract void onAIHandleGeneralEvent(AbstractAI ai, AIEventType eventType);
}
