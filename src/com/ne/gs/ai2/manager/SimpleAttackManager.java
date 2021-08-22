/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.manager;

import com.ne.gs.ai2.AI2Logger;
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.AbstractAI;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.ThreadPoolManager;
import mw.engines.geo.GeoHelper;

/**
 * @author ATracer
 */
public final class SimpleAttackManager {

    /**
     * @param npcAI
     * @param delay
     */
    public static void performAttack(NpcAI2 npcAI, int delay) {
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "performAttack");
        }

        if (npcAI.getOwner().getGameStats().isNextAttackScheduled()) {
            if (npcAI.isLogging()) {
                AI2Logger.info(npcAI, "Attack already sheduled");
            }
            scheduleCheckedAttackAction(npcAI, delay);
            return;
        }

        if (!isTargetInAttackRange(npcAI.getOwner())) {
            if (npcAI.isLogging()) {
                AI2Logger.info(npcAI, "Attack will not be scheduled because of range");
            }
            npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
            return;
        }
        npcAI.getOwner().getGameStats().setNextAttackTime(System.currentTimeMillis() + delay);
        if (delay > 0) {
            ThreadPoolManager.getInstance().schedule(new SimpleAttackAction(npcAI), delay);
        } else {
            attackAction(npcAI);
        }
    }

    /**
     * @param npcAI
     * @param delay
     */
    private static void scheduleCheckedAttackAction(NpcAI2 npcAI, int delay) {
        if (delay < 2000) {
            delay = 2000;
        }
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "Scheduling checked attack " + delay);
        }
        ThreadPoolManager.getInstance().schedule(new SimpleCheckedAttackAction(npcAI), delay);
    }

    public static boolean isTargetInAttackRange(Npc npc) {
        if (npc.getAi2().isLogging()) {
            float distance = npc.getDistanceToTarget();
            AI2Logger.info((AbstractAI) npc.getAi2(), "isTargetInAttackRange: " + distance);
        }

        VisibleObject target = npc.getTarget();
        if (target == null || !(target instanceof Creature)) {
            return false;
        }


        boolean cansee = GeoHelper.canSee(npc, target);

        float collision = npc.getObjectTemplate().getBoundRadius().getCollision() + target.getObjectTemplate().getBoundRadius().getCollision();

        return cansee && MathUtil.isInAttackRange(npc,
                (Creature) npc.getTarget(),
                Math.max(npc.getGameStats().getAttackRange().getCurrent() / 1000f, collision));
        /*
        float col = Math.max(npc.getObjectTemplate().getBoundRadius().getCollision(), npc.getGameStats().getAttackRange().getCurrent() / 1000f)
                + target.getObjectTemplate().getBoundRadius().getCollision();

        return cansee && MathUtil.isInAttackRange(npc, (Creature) target, col);
        */
        // return distance <= npc.getController().getAttackDistanceToTarget() + NpcMoveController.MOVE_CHECK_OFFSET;
    }

    /**
     * @param npcAI
     */
    protected static void attackAction(NpcAI2 npcAI) {
        if (!npcAI.isInState(AIState.FIGHT)) {
            return;
        }
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "attackAction");
        }
        Npc npc = npcAI.getOwner();
        Creature target = (Creature) npc.getTarget();
        if (target != null && !target.getLifeStats().isAlreadyDead()) {
            if (!npc.canSee(target)) {
                npcAI.onGeneralEvent(AIEventType.TARGET_GIVEUP);
                return;
            }
            if (isTargetInAttackRange(npc) && GeoHelper.canSee(npc, target)) {
                npc.getController().attackTarget(target, 0);
                npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
                return;
            }
            npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
        } else {
            npcAI.onGeneralEvent(AIEventType.TARGET_GIVEUP);
        }
    }

    private final static class SimpleAttackAction implements Runnable {

        private NpcAI2 npcAI;

        SimpleAttackAction(NpcAI2 npcAI) {
            this.npcAI = npcAI;
        }

        @Override
        public void run() {
            attackAction(npcAI);
            npcAI = null;
        }
    }

    private final static class SimpleCheckedAttackAction implements Runnable {

        private NpcAI2 npcAI;

        SimpleCheckedAttackAction(NpcAI2 npcAI) {
            this.npcAI = npcAI;
        }

        @Override
        public void run() {
            if (!npcAI.getOwner().getGameStats().isNextAttackScheduled()) {
                attackAction(npcAI);
            } else if (npcAI.isLogging()) {
                AI2Logger.info(npcAI, "Scheduled checked attacked confirmed");
            }
            npcAI = null;
        }
    }
}
