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
import com.ne.gs.model.NpcType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.utils.MathUtil;
import mw.engines.geo.GeoHelper;
public final class SimpleAbyssGuardHandler {
    public static void onCreatureMoved(NpcAI2 npcAI, Creature creature) {
        checkAggro(npcAI, creature);
    }
    public static void onCreatureSee(NpcAI2 npcAI, Creature creature) {
        checkAggro(npcAI, creature);
    }
    protected static void checkAggro(NpcAI2 ai, Creature creature) {
        if (!(creature instanceof Npc)) {
            CreatureEventHandler.checkAggro(ai, creature);
            return;
        }
        Npc owner = ai.getOwner();
        if (creature.getLifeStats().isAlreadyDead() || !owner.canSee(creature)) {
            return;
        }
        NpcTemplate template = ((Npc) creature).getObjectTemplate();
        if (template.getNpcType() != NpcType.ATTACKABLE && template.getNpcType() != NpcType.AGGRESSIVE || template.getLevel() < 2) {
            return;
        }
        if (creature.getTarget() != null) {
            return;
        }
        if (!owner.getActiveRegion().isMapRegionActive()) {
            return;
        }
        if (!ai.isInState(AIState.FIGHT) && MathUtil.isIn3dRange(owner, creature, owner.getObjectTemplate().getAggroRange())) {
            if (GeoHelper.canSee(owner, creature)) {
                if (!ai.isInState(AIState.RETURNING)) {
                    ai.getOwner().getMoveController().storeStep();
                }
                ai.onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
            }
        }
    }
}