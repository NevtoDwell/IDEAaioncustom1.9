/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.poll;

import com.ne.gs.ai2.NpcAI2;

/**
 * @author ATracer
 */
public final class NpcAIPolls {

    /**
     * @param npcAI
     */
    public static AIAnswer shouldDecay(NpcAI2 npcAI) {
        return AIAnswers.POSITIVE;
    }

    /**
     * @param npcAI
     *
     * @return
     */
    public static AIAnswer shouldRespawn(NpcAI2 npcAI) {
        return AIAnswers.POSITIVE;
    }

}
