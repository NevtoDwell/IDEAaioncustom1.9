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
import com.ne.gs.ai2.AbstractAI;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.manager.EmoteManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer
 */
public final class FollowEventHandler {

    /**
     * @param npcAI
     * @param creature
     */
    public static void follow(NpcAI2 npcAI, Creature creature) {
        if (npcAI.setStateIfNot(AIState.FOLLOWING)) {
            npcAI.getOwner().setTarget(creature);
            EmoteManager.emoteStartFollowing(npcAI.getOwner());
        }
    }

    /**
     * @param npcAI
     * @param creature
     */
    public static void creatureMoved(NpcAI2 npcAI, Creature creature) {
        if (npcAI.isInState(AIState.FOLLOWING)) {
            if (npcAI.getOwner().isTargeting(creature.getObjectId()) && !creature.getLifeStats().isAlreadyDead()) {
                checkFollowTarget(npcAI, creature);
            }
        }
    }

    /**
     * @param creature
     */
    public static void checkFollowTarget(NpcAI2 npcAI, Creature creature) {
        if (!isInRange(npcAI, creature)) {
            npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
        }
    }

    public static boolean isInRange(AbstractAI ai, VisibleObject object) {
        if (object == null) {
            return false;
        }

        return MathUtil.isIn3dRange(ai.getOwner(), object, 2 + object.getObjectTemplate().getBoundRadius().getCollision());
    }

    /**
     * @param npcAI
     * @param creature
     */
    public static void stopFollow(NpcAI2 npcAI, Creature creature) {
        if (npcAI.setStateIfNot(AIState.IDLE)) {
            npcAI.getOwner().setTarget(null);
            npcAI.getOwner().getMoveController().abortMove();
            npcAI.getOwner().getController().scheduleRespawn();
            npcAI.getOwner().getController().onDelete();
        }
    }
}
