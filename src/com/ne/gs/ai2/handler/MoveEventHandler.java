/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.handler;

import com.ne.gs.ai2.NpcAI2;

/**
 * @author ATracer
 */
public final class MoveEventHandler {

    /**
     * @param npcAI
     */
    public static void onMoveValidate(NpcAI2 npcAI) {
        npcAI.getOwner().getController().onMove();
        TargetEventHandler.onTargetTooFar(npcAI);
    }

    /**
     * @param npcAI
     */
    public static void onMoveArrived(NpcAI2 npcAI) {
        npcAI.getOwner().getController().onMove();
        TargetEventHandler.onTargetReached(npcAI);
    }
}
