/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.league;

import com.ne.gs.model.team2.TeamMember;
import com.ne.gs.model.team2.alliance.PlayerAlliance;

/**
 * @author ATracer
 */
public class LeagueMember implements TeamMember<PlayerAlliance> {

    private final PlayerAlliance alliance;
    private final int leaguePosition;

    public LeagueMember(PlayerAlliance alliance, int position) {
        this.alliance = alliance;
        leaguePosition = position;
    }

    @Override
    public Integer getObjectId() {
        return alliance.getObjectId();
    }

    @Override
    public String getName() {
        return alliance.getName();
    }

    @Override
    public PlayerAlliance getObject() {
        return alliance;
    }

    public final int getLeaguePosition() {
        return leaguePosition;
    }

}
