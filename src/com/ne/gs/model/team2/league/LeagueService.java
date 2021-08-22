/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.league;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple1;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.callback.AllianceCallbacks;
import com.ne.gs.model.team2.league.events.LeagueDisbandEvent;
import com.ne.gs.model.team2.league.events.LeagueEnteredEvent;
import com.ne.gs.model.team2.league.events.LeagueInvite;
import com.ne.gs.model.team2.league.events.LeagueLeftEvent;
import com.ne.gs.model.team2.league.events.LeagueLeftEvent.LeaveReson;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public final class LeagueService {

    private static final Logger log = LoggerFactory.getLogger(LeagueService.class);

    private static final Map<Integer, League> leagues = new ConcurrentHashMap<>();

    static {
        EventNotifier.GLOBAL.attach(new AfterDisband());
    }

    public static void inviteToLeague(Player inviter, Player invited) {
        if (canInvite(inviter, invited)) {
            LeagueInvite invite = new LeagueInvite(inviter, invited);
            if (invited.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_MSGBOX_UNION_INVITE_ME, invite)) {
                inviter.sendPck(SM_SYSTEM_MESSAGE.STR_UNION_INVITE_HIM(invited.getName(), invited.getPlayerAlliance2().size()));
                invited.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_MSGBOX_UNION_INVITE_ME, 0, 0, inviter.getName()));
            }
        }
    }

    public static boolean canInvite(Player inviter, Player invited) {
        return inviter.isInAlliance2() && invited.isInAlliance2() && inviter.getPlayerAlliance2().isLeader(inviter);
    }

    public static League createLeague(Player inviter, Player invited) {
        PlayerAlliance alliance = inviter.getPlayerAlliance2();
        Preconditions.checkNotNull(alliance, "Alliance can not be null");
        League newLeague = new League(new LeagueMember(alliance, 0));
        leagues.put(newLeague.getTeamId(), newLeague);
        addAlliance(newLeague, alliance);
        return newLeague;
    }

    /**
     * Add alliance to league
     */
    public static void addAlliance(League league, PlayerAlliance alliance) {
        Preconditions.checkNotNull(league, "League should not be null");
        league.onEvent(new LeagueEnteredEvent(league, alliance));
    }

    public static void addAllianceToLeague(League league, PlayerAlliance alliance) {
        league.addMember(new LeagueMember(alliance, league.size()));
    }

    /**
     * Remove alliance from league (normal leave)
     */
    public static void removeAlliance(PlayerAlliance alliance) {
        if (alliance != null) {
            League league = alliance.getLeague();
            Preconditions.checkNotNull(league, "League should not be null");
            league.onEvent(new LeagueLeftEvent(league, alliance));
        }
    }

    /**
     * Remove alliance from league (expel)
     */
    public static void expelAlliance(Player expelledPlayer, Player expelGiver) {
        Preconditions.checkNotNull(expelledPlayer, "Expelled player should not be null");
        Preconditions.checkNotNull(expelGiver, "ExpelGiver player should not be null");
        Preconditions.checkArgument(expelGiver.isInLeague(), "Expelled player should be in league");
        Preconditions.checkArgument(expelledPlayer.isInLeague(), "ExpelGiver should be in league");
        Preconditions.checkArgument(expelGiver.getPlayerAlliance2().getLeague().isLeader(expelGiver.getPlayerAlliance2()),
            "ExpelGiver alliance should be the leader of league");
        Preconditions.checkArgument(expelGiver.getPlayerAlliance2().isLeader(expelGiver), "ExpelGiver should be the leader of alliance");
        PlayerAlliance alliance = expelGiver.getPlayerAlliance2();
        League league = alliance.getLeague();
        league.onEvent(new LeagueLeftEvent(league, expelledPlayer.getPlayerAlliance2(), LeaveReson.EXPEL));
    }

    /**
     * Disband league after minimum of members has been reached
     */
    public static void disband(League league) {
        Preconditions.checkState(league.onlineMembers() <= 1, "Can't disband league with more than one online member");
        leagues.remove(league.getTeamId());
        league.onEvent(new LeagueDisbandEvent(league));
    }

    static class AfterDisband extends AllianceCallbacks.AfterDisband {
        @Override
        public Object onEvent(@NotNull PlayerAlliance e) {
            try {
                for (League league : leagues.values()) {
                    if (league.hasMember(e.getTeamId())) {
                        league.onEvent(new LeagueLeftEvent(league, e));
                    }
                }
            } catch (Throwable t) {
                log.error("Error during alliance disband listen", t);
            }
            return null;
        }
    }

}
