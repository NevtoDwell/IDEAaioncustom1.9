/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.scenario;

import com.ne.gs.ai2.AbstractAI;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public interface AI2Scenario {

    void onCreatureEvent(AbstractAI ai, AIEventType event, Creature creature);

    void onGeneralEvent(AbstractAI ai, AIEventType event);
}
