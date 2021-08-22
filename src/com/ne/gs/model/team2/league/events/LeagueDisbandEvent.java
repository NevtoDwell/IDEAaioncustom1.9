/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.league.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.league.League;
import com.ne.gs.model.team2.league.events.LeagueLeftEvent.LeaveReson;

/**
 * @author ATracer
 */
public class LeagueDisbandEvent extends AlwaysTrueTeamEvent implements Predicate<PlayerAlliance> {

    private final League league;

    public LeagueDisbandEvent(League league) {
        this.league = league;
    }

    @Override
    public void handleEvent() {
        league.applyOnMembers(this);
    }

    @Override
    public boolean apply(PlayerAlliance alliance) {
        league.onEvent(new LeagueLeftEvent(league, alliance, LeaveReson.DISBAND));
        return true;
    }

}
