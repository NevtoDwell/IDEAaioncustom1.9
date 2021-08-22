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
import com.ne.gs.ai2.AISubState;
import com.ne.gs.ai2.AbstractAI;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.manager.WalkManager;
import com.ne.gs.model.gameobjects.Npc;

public final class FreezeEventHandler {

    public static void onUnfreeze(AbstractAI ai) {
        if (ai.isInSubState(AISubState.FREEZE)) {
            ai.setSubStateIfNot(AISubState.NONE);
            if (ai instanceof NpcAI2) {
                Npc npc = ((NpcAI2) ai).getOwner();
                if (npc.getWalkerGroup() != null) {
                    ai.setStateIfNot(AIState.WALKING);
                    ai.setSubStateIfNot(AISubState.WALK_WAIT_GROUP);
                } else if (npc.getSpawn().getRandomWalk() > 0) {
                    ai.setStateIfNot(AIState.WALKING);
                    ai.setSubStateIfNot(AISubState.WALK_RANDOM);
                }
                npc.updateKnownlist();
            }
            ai.think();
        }
    }

    public static void onFreeze(AbstractAI ai) {
        if (ai.isInState(AIState.WALKING)) {
            WalkManager.stopWalking((NpcAI2) ai);
        }
        ai.setStateIfNot(AIState.IDLE);
        ai.setSubStateIfNot(AISubState.FREEZE);
        ai.think();
        if (ai instanceof NpcAI2) {
            Npc npc = ((NpcAI2) ai).getOwner();
            npc.updateKnownlist();
            npc.getAggroList().clear();
            npc.getEffectController().removeAllEffects();
        }
    }
}
