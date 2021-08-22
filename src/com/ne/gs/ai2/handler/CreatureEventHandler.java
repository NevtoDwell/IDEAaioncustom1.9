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
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.utils.MathUtil;
import mw.engines.geo.GeoHelper;
/**
 * @author ATracer
 */
public final class CreatureEventHandler {
    /**
     * @param npcAI
     * @param creature
     */
    public static void onCreatureMoved(NpcAI2 npcAI, Creature creature) {
        checkAggro(npcAI, creature);
    }
    /**
     * @param npcAI
     * @param creature
     */
    public static void onCreatureSee(NpcAI2 npcAI, Creature creature) {
        checkAggro(npcAI, creature);
    }
    /**
     * @param ai
     * @param creature
     */
    protected static void checkAggro(NpcAI2 ai, Creature creature) {
        Npc owner = ai.getOwner();
        if (creature.getLifeStats().isAlreadyDead()) {
            return;
        }
        if (!owner.canSee(creature)) {
            return;
        }
        if (!owner.getActiveRegion().isMapRegionActive()) {
            return;
        }
        boolean isInAggroRange = false;
        if (ai.poll(AIQuestion.CAN_SHOUT)) {
            int shoutRange = owner.getObjectTemplate().getMinimumShoutRange();
            double distance = MathUtil.getDistance(owner, creature);
            if (distance <= shoutRange) {
                ShoutEventHandler.onSee(ai, creature);
                isInAggroRange = shoutRange <= owner.getObjectTemplate().getAggroRange();
            }
        }
        if (!ai.isInState(AIState.FIGHT) && (isInAggroRange || MathUtil.isIn3dRange(owner, creature, owner.getObjectTemplate().getAggroRange()))) {
            if (owner.isAggressiveTo(creature)) {
                if (!ai.isInState(AIState.RETURNING)) {
                    ai.getOwner().getMoveController().storeStep();
                }
                if (ai.canThink() && GeoHelper.canSee(owner, creature)) {
                    ai.onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
                }
            }
        }
    }
}