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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.league.League;
import com.ne.gs.model.team2.league.LeagueMember;
import com.ne.gs.model.team2.league.LeagueService;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;

/**
 * @author ATracer
 */
public class LeagueLeftEvent extends AlwaysTrueTeamEvent implements Predicate<LeagueMember> {

    private final League league;
    private final PlayerAlliance alliance;
    private final LeaveReson reason;

    public static enum LeaveReson {
        LEAVE,
        EXPEL,
        DISBAND;
    }

    public LeagueLeftEvent(League league, PlayerAlliance alliance) {
        this(league, alliance, LeaveReson.LEAVE);
    }

    public LeagueLeftEvent(League league, PlayerAlliance alliance, LeaveReson reason) {
        this.league = league;
        this.alliance = alliance;
        this.reason = reason;
    }

    @Override
    public void handleEvent() {
        league.removeMember(alliance.getTeamId());
        league.apply(this);

        switch (reason) {
            case LEAVE:
                alliance.sendPacket(new SM_ALLIANCE_INFO(alliance));
                checkDisband();
                break;
            case EXPEL:
                // TODO getLeaderName in team2
                alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_EXPELLED, league.getLeaderObject().getLeader().getName()));
                checkDisband();
                break;
            case DISBAND:
                alliance.sendPacket(new SM_ALLIANCE_INFO(alliance));
                break;
        }
    }

    private final void checkDisband() {
        if (league.onlineMembers() <= 1) {
            LeagueService.disband(league);
        }
    }

    @Override
    public boolean apply(LeagueMember member) {

        PlayerAlliance leagueAlliance = member.getObject();
        leagueAlliance.applyOnMembers(new Predicate<Player>() {

            @Override
            public boolean apply(Player member) {
                switch (reason) {
                    case LEAVE:
                        member.sendPck(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_LEFT, alliance.getLeader().getName()));
                        break;
                    case EXPEL:
                        // TODO may be EXPEL message only to leader
                        member.sendPck(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_EXPEL, alliance.getLeader().getName()));
                        break;
                }
                return true;
            }

        });

        return true;
    }
}
