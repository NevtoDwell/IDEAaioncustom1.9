/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.handler;
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.NpcAI2;
/**
 * @author ATracer
 */
public final class SpawnEventHandler {
    /**
     * @param npcAI
     */
    public static void onSpawn(NpcAI2 npcAI) {
        if (npcAI.setStateIfNot(AIState.IDLE)) {
            if (npcAI.getOwner().getPosition().isMapRegionActive()) {
                npcAI.think();
            }
        }
    }
    /**
     * @param npcAI
     */
    public static void onDespawn(NpcAI2 npcAI) {
        npcAI.setStateIfNot(AIState.DESPAWNED);
    }
    /**
     * @param npcAI
     */
    public static void onRespawn(NpcAI2 npcAI) {
        npcAI.getOwner().getMoveController().resetMove();
    }
}