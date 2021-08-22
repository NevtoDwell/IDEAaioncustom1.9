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
import com.ne.gs.ai2.manager.WalkManager;
import com.ne.gs.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public final class ActivateEventHandler {

    public static void onActivate(NpcAI2 npcAI) {
        if (npcAI.isInState(AIState.IDLE)) {
            npcAI.getOwner().updateKnownlist();
            npcAI.think();
        }
    }

    public static void onDeactivate(NpcAI2 npcAI) {
        if (npcAI.isInState(AIState.WALKING)) {
            WalkManager.stopWalking(npcAI);
        }
        npcAI.think();
        Npc npc = npcAI.getOwner();
        //npc.clearKnownlist(); // seems to cause respawn issues
        npc.updateKnownlist();
        npc.getAggroList().clear();
        npc.getEffectController().removeAllEffects();
    }
}
