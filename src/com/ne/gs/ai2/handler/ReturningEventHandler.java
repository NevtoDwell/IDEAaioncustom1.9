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
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.manager.EmoteManager;
import com.ne.gs.ai2.manager.WalkManager;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.geometry.Point3D;
/**
 * @author ATracer
 */
public final class ReturningEventHandler {
    /**
     * @param npcAI
     */
    public static void onNotAtHome(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onNotAtHome");
        }
        if (npcAI.setStateIfNot(AIState.RETURNING)) {
            if (npcAI.isLogging()) {
                AI2Logger.info(npcAI, "returning and restoring");
            }
            EmoteManager.emoteStartReturning(npcAI.getOwner());
            npcAI.getOwner().getLifeStats().triggerRestoreTask();
            Point3D prevStep = npcAI.getOwner().getMoveController().recallPreviousStep();

           //npcAI.getOwner().getMoveController().abortMove();
            npcAI.getOwner().getMoveController().moveToPoint(prevStep.getX(), prevStep.getY(), prevStep.getZ());
        }
    }
    /**
     * @param npcAI
     */
    public static void onBackHome(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onBackHome");
        }
        npcAI.getOwner().getMoveController().clearBackSteps();
        if (npcAI.setStateIfNot(AIState.IDLE)) {
            EmoteManager.emoteStartIdling(npcAI.getOwner());
            ThinkEventHandler.thinkIdle(npcAI);
        }
        if (npcAI.getOwner().hasWalkRoutes()) {
            WalkManager.startWalking(npcAI);
        }
    }
}