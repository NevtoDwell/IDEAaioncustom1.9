/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.events;

import com.ne.gs.model.team2.TeamEvent;

/**
 * @author ATracer
 */
public abstract class AlwaysTrueTeamEvent implements TeamEvent {

    @Override
    public final boolean checkCondition() {
        return true;
    }

}
