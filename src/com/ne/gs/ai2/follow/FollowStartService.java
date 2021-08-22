/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.follow;

import java.util.concurrent.Future;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.utils.ThreadPoolManager;

public final class FollowStartService {

    public static Future<?> newFollowingToTargetCheckTask(Summon follower, Creature leading) {
        return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FollowSummonTaskAI(leading, follower), 1000, 1000);
    }
}
