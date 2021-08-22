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
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.manager.WalkManager;
import com.ne.gs.controllers.movement.NpcMoveController;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.geometry.Point3D;
import com.ne.gs.utils.MathUtil;
/**
 * @author ATracer
 */
public final class ThinkEventHandler {
    /**
     * Maximum distance between npc position and spawn location to trigger return to home event
     */
    public static final int MAXIMUM_LEAVE_DISTANCE = 40;
    /**
     * @param npcAI
     */
    public static void onThink(NpcAI2 npcAI) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "think");
        }
        if (npcAI.isAlreadyDead()) {
            AI2Logger.info(npcAI, "can't think in dead state");
            return;
        }
        if (!npcAI.tryLockThink()) {
            AI2Logger.info(npcAI, "can't acquire lock");
            return;
        }
        try {
            if (!npcAI.getOwner().getPosition().isMapRegionActive() || npcAI.getSubState() == AISubState.FREEZE) {
                thinkInInactiveRegion(npcAI);
                return;
            }
            if (npcAI.isLogging()) {
                AI2Logger.info(npcAI, "think state " + npcAI.getState());
            }
            switch (npcAI.getState()) {
                case FIGHT:
                    thinkAttack(npcAI);
                    break;
                case WALKING:
                    thinkWalking(npcAI);
                    break;
                case IDLE:
                    thinkIdle(npcAI);
                    break;
            }
        } finally {
            npcAI.unlockThink();
        }
    }
    /**
     * @param npcAI
     */
    private static void thinkInInactiveRegion(NpcAI2 npcAI) {
        if (!npcAI.canThink()) {
            return;
        }
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "think in inactive region: " + npcAI.getState());
        }
        switch (npcAI.getState()) {
            case FIGHT:
                thinkAttack(npcAI);
                break;
            default:
                if (!npcAI.getOwner().isAtSpawnLocation()) {
                    npcAI.onGeneralEvent(AIEventType.NOT_AT_HOME);
                }
        }
    }
    /**
     * @param npcAI
     */
    public static void thinkAttack(NpcAI2 npcAI) {
        Npc npc = npcAI.getOwner();
        Creature mostHated = npc.getAggroList().getMostHated();
        if (mostHated != null && !mostHated.getLifeStats().isAlreadyDead() && stillCanAttack(npc)) {
            npcAI.onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
        } else {
            npc.getMoveController().recallPreviousStep();
            npcAI.onGeneralEvent(AIEventType.ATTACK_FINISH);
            npcAI.onGeneralEvent(npc.isAtSpawnLocation() ? AIEventType.BACK_HOME : AIEventType.NOT_AT_HOME);
        }
    }
    private static boolean stillCanAttack(Npc npc){
        Point3D step = npc.getMoveController().getPreviousStep();
        return MathUtil.getDistance(npc, step.getX(), step.getY(), step.getZ()) < MAXIMUM_LEAVE_DISTANCE;
    }
    /**
     * @param npcAI
     */
    public static void thinkWalking(NpcAI2 npcAI) {
        WalkManager.startWalking(npcAI);
    }
    /**
     * @param npcAI
     */
    public static void thinkIdle(NpcAI2 npcAI) {
        if (WalkManager.isWalking(npcAI)) {
            boolean startedWalking = WalkManager.startWalking(npcAI);
            if (!startedWalking) {
                npcAI.setStateIfNot(AIState.IDLE);
            }
        }
    }
}