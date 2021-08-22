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
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public final class FollowManager {

    public static void targetTooFar(NpcAI2 npcAI) {
        Npc npc = npcAI.getOwner();
        if (npcAI.isLogging()) {
            AI2Logger.info(npcAI, "Follow manager - targetTooFar");
        }
        if (npcAI.isMoveSupported() && !npc.getEffectController().isUnderFear()) {
            npc.getMoveController().moveToTargetObject();
        }
    }
}
