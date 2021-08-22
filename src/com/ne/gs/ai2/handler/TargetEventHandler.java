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
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.manager.AttackManager;
import com.ne.gs.ai2.manager.FollowManager;
import com.ne.gs.ai2.manager.WalkManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;
/**
 * @author ATracer
 */
public final class TargetEventHandler {
    /**
     * @param npcAI
     */
    public static void onTargetReached(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onTargetReached");
        }
        AIState currentState = npcAI.getState();
        switch (currentState) {
            case FIGHT:
                npcAI.getOwner().getMoveController().abortMove();
                AttackManager.scheduleNextAttack(npcAI);
                if (npcAI.getOwner().getMoveController().isFollowingTarget()) {
                    npcAI.getOwner().getMoveController().storeStep();
                }
                break;
            case RETURNING:
                npcAI.getOwner().getMoveController().abortMove();
                npcAI.getOwner().getMoveController().recallPreviousStep();
                if (npcAI.getOwner().isAtSpawnLocation()) {
                    npcAI.onGeneralEvent(AIEventType.BACK_HOME);
                } else {
                    npcAI.onGeneralEvent(AIEventType.NOT_AT_HOME);
                }
                break;
            case WALKING:
                WalkManager.targetReached(npcAI);
                break;
            case FOLLOWING:
                npcAI.getOwner().getMoveController().abortMove();
                npcAI.getOwner().getMoveController().storeStep();
                break;
            case FEAR:// TODO remove this state
                npcAI.getOwner().getMoveController().abortMove();
                npcAI.getOwner().getMoveController().storeStep();
                break;
        }
    }
    /**
     * @param npcAI
     */
    public static void onTargetTooFar(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onTargetTooFar");
        }
        switch (npcAI.getState()) {
            case FIGHT:
                AttackManager.targetTooFar(npcAI);
                break;
            case FOLLOWING:
                FollowManager.targetTooFar(npcAI);
                break;
            case FEAR:
                break;
            default:
                if (npcAI.isLogging()) {
                    AI2Logger.info(npcAI, "default onTargetTooFar");
                }
        }
    }
    /**
     * @param npcAI
     */
    public static void onTargetGiveup(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onTargetGiveup");
        }
        VisibleObject target = npcAI.getOwner().getTarget();
        if (target != null) {
            npcAI.getOwner().getAggroList().stopHating(target);
        }
        if (npcAI.isMoveSupported()) {
            npcAI.getOwner().getMoveController().abortMove();
        }
        if (!npcAI.isAlreadyDead()) {
            npcAI.think();
        }
    }
    /**
     * @param npcAI
     */
    public static void onTargetChange(NpcAI2 npcAI, Creature creature) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "onTargetChange");
        }
        if (npcAI.isInState(AIState.FIGHT)) {
            npcAI.getOwner().setTarget(creature);
            AttackManager.scheduleNextAttack(npcAI);
        }
    }
}