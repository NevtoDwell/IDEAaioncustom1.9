/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.handler;

import com.ne.gs.ai2.AI2Logger;
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.AISubState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public final class DiedEventHandler {

    public static void onDie(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onDie");
        }

        onSimpleDie(npcAI);

        Npc owner = npcAI.getOwner();
        owner.setTarget(null);
    }

    public static void onSimpleDie(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onSimpleDie");
        }

        if (npcAI.poll(AIQuestion.CAN_SHOUT)) {
            ShoutEventHandler.onDied(npcAI);
        }

        npcAI.setStateIfNot(AIState.DIED);
        npcAI.setSubStateIfNot(AISubState.NONE);
        npcAI.getOwner().getAggroList().clear();
    }

}
