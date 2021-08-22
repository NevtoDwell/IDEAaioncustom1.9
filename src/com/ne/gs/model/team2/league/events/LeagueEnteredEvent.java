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

import com.ne.gs.model.team2.TeamEvent;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.league.League;
import com.ne.gs.model.team2.league.LeagueMember;
import com.ne.gs.model.team2.league.LeagueService;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SHOW_BRAND;

/**
 * @author ATracer
 */
public class LeagueEnteredEvent implements Predicate<LeagueMember>, TeamEvent {

    private final League league;
    private final PlayerAlliance invitedAlliance;

    public LeagueEnteredEvent(League league, PlayerAlliance alliance) {
        this.league = league;
        invitedAlliance = alliance;
    }

    /**
     * Entered alliance should not be in league yet
     */
    @Override
    public boolean checkCondition() {
        return !league.hasMember(invitedAlliance.getObjectId());
    }

    @Override
    public void handleEvent() {
        LeagueService.addAllianceToLeague(league, invitedAlliance);
        league.apply(this);
    }

    @Override
    public boolean apply(LeagueMember member) {
        PlayerAlliance alliance = member.getObject();
        alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_ENTERED, league.getLeaderObject().getLeader().getName()));
        alliance.sendPacket(new SM_SHOW_BRAND(0, 0));
        return true;
    }

}
