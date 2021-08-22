/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.callback.AllianceCallbacks;
import com.ne.gs.model.team2.alliance.events.*;
import com.ne.gs.model.team2.alliance.events.AssignViceCaptainEvent.AssignType;
import com.ne.gs.model.team2.common.events.PlayerLeavedEvent.LeaveReson;
import com.ne.gs.model.team2.common.events.ShowBrandEvent;
import com.ne.gs.model.team2.common.events.TeamCommand;
import com.ne.gs.model.team2.common.events.TeamKinahDistributionEvent;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.TimeUtil;

/**
 * @author ATracer
 */
public final class PlayerAllianceService {

    private static final Logger log = LoggerFactory.getLogger(PlayerAllianceService.class);

    private static final Map<Integer, PlayerAlliance> alliances = new ConcurrentHashMap<>();
    private static final AtomicBoolean offlineCheckStarted = new AtomicBoolean();

    public static void inviteToAlliance(Player inviter, Player invited) {
        if (canInvite(inviter, invited)) {
            PlayerAllianceInvite invite = new PlayerAllianceInvite(inviter, invited);
            if (invited.getResponseRequester()
                       .putRequest(SM_QUESTION_WINDOW.STR_PARTY_ALLIANCE_DO_YOU_ACCEPT_HIS_INVITATION, invite)) {
                if (invited.isInGroup2()) {
                    inviter.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_INVITED_HIS_PARTY(invited.getName()));
                } else {
                    inviter.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_INVITED_HIM(invited.getName()));
                }
                invited.sendPck(
                    new SM_QUESTION_WINDOW(
                        SM_QUESTION_WINDOW.STR_PARTY_ALLIANCE_DO_YOU_ACCEPT_HIS_INVITATION, 0, 0, inviter.getName()));
            }
        }
    }

    public static boolean canInvite(Player inviter, Player invited) {
        return RestrictionsManager.canInviteToAlliance(inviter, invited);
    }

    public static PlayerAlliance createAlliance(Player leader, Player invited) {
        EventNotifier.GLOBAL.fire(AllianceCallbacks.BeforeCreate.class, leader);

        PlayerAlliance newAlliance = new PlayerAlliance(new PlayerAllianceMember(leader));
        alliances.put(newAlliance.getTeamId(), newAlliance);
        addPlayer(newAlliance, leader);
        if (offlineCheckStarted.compareAndSet(false, true)) {
            initializeOfflineCheck();
        }

        EventNotifier.GLOBAL.fire(AllianceCallbacks.AfterCreate.class, leader);
        return newAlliance;
    }

    private static void initializeOfflineCheck() {
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new OfflinePlayerAllianceChecker(), 1000, 30 * 1000);
    }

    public static void addPlayerToAlliance(PlayerAlliance alliance, Player invited) {
        // TODO leader member is already set
        EventNotifier.GLOBAL.fire(AllianceCallbacks.BeforeEnter.class, Tuple2.of(alliance, invited));

        alliance.addMember(new PlayerAllianceMember(invited));

        EventNotifier.GLOBAL.fire(AllianceCallbacks.AfterEnter.class, Tuple2.of(alliance, invited));
    }

    /**
     * Change alliance's loot rules and notify team members
     */
    public static void changeGroupRules(PlayerAlliance alliance, LootGroupRules lootRules) {
        alliance.onEvent(new ChangeAllianceLootRulesEvent(alliance, lootRules));
    }

    /**
     * Player entered world - search for non expired alliance
     */
    public static void onPlayerLogin(Player player) {
        for (PlayerAlliance alliance : alliances.values()) {
            PlayerAllianceMember member = alliance.getMember(player.getObjectId());
            if (member != null) {
                alliance.onEvent(new PlayerConnectedEvent(alliance, player));
            }
        }
    }

    /**
     * Player leaved world - set last online on member
     */
    public static void onPlayerLogout(Player player) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            PlayerAllianceMember member = alliance.getMember(player.getObjectId());
            member.updateLastOnlineTime();
            alliance.onEvent(new PlayerDisconnectedEvent(alliance, player));
        }
    }

    /**
     * Update alliance members to some event of player
     */
    public static void updateAlliance(Player player, PlayerAllianceEvent allianceEvent) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            alliance.onEvent(new PlayerAllianceUpdateEvent(alliance, player, allianceEvent));
        }
    }

    /**
     * Add player to alliance
     */
    public static void addPlayer(PlayerAlliance alliance, Player player) {
        Preconditions.checkNotNull(alliance, "Alliance should not be null");
        alliance.onEvent(new PlayerEnteredEvent(alliance, player));
    }

    /**
     * Remove player from alliance (normal leave, or kick offline player)
     */
    public static void removePlayer(Player player) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            alliance.onEvent(new PlayerAllianceLeavedEvent(alliance, player));
        }
    }

    /**
     * Remove player from alliance (ban)
     */
    public static void banPlayer(Player bannedPlayer, Player banGiver) {
        Preconditions.checkNotNull(bannedPlayer, "Banned player should not be null");
        Preconditions.checkNotNull(banGiver, "Bangiver player should not be null");
        PlayerAlliance alliance = banGiver.getPlayerAlliance2();
        if (alliance != null) {
            PlayerAllianceMember bannedMember = alliance.getMember(bannedPlayer.getObjectId());
            if (bannedMember != null) {
                alliance.onEvent(
                    new PlayerAllianceLeavedEvent(
                        alliance, bannedMember.getObject(), LeaveReson.BAN, banGiver.getName()));
            } else {
                log.warn("TEAM2: banning player not in alliance {}", alliance.onlineMembers());
            }
        }
    }

    /**
     * Disband alliance after minimum of members has been reached
     */
    public static void disband(PlayerAlliance alliance) {
        EventNotifier.GLOBAL.fire(AllianceCallbacks.BeforeDisband.class, alliance);

        Preconditions.checkState(alliance.onlineMembers() <= 1, "Can't disband alliance with more than one online member");
        alliances.remove(alliance.getTeamId());
        alliance.onEvent(new AllianceDisbandEvent(alliance));

        EventNotifier.GLOBAL.fire(AllianceCallbacks.AfterDisband.class, alliance);
    }

    public static void changeLeader(Player player) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            alliance.onEvent(new ChangeAllianceLeaderEvent(alliance, player));
        }
    }

    /**
     * Change vice captain position of player (promote, demote)
     */
    public static void changeViceCaptain(Player player, AssignType assignType) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            alliance.onEvent(new AssignViceCaptainEvent(alliance, player, assignType));
        }
    }

    public static PlayerAlliance searchAlliance(Integer playerObjId) {
        for (PlayerAlliance alliance : alliances.values()) {
            if (alliance.hasMember(playerObjId)) {
                return alliance;
            }
        }
        return null;
    }

    /**
     * Move members between alliance groups
     */
    public static void changeMemberGroup(Player player, int firstPlayer, int secondPlayer, int allianceGroupId) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        Preconditions.checkNotNull(alliance, "Alliance should not be null for group change");
        if (alliance.isLeader(player) || alliance.isViceCaptain(player)) {
            alliance.onEvent(new ChangeMemberGroupEvent(alliance, firstPlayer, secondPlayer, allianceGroupId));
        } else {
            player.sendMsg("You do not have the authority for that.");
        }
    }

    /**
     * Check that alliance is ready
     */
    public static void checkReady(Player player, TeamCommand eventCode) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            alliance.onEvent(new CheckAllianceReadyEvent(alliance, player, eventCode));
        }
    }

    /**
     * Share specific amount of kinah between alliance members
     */
    public static void distributeKinah(Player player, long amount) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            alliance.onEvent(new TeamKinahDistributionEvent<>(alliance, player, amount));
        }
    }

    public static void distributeKinahInGroup(Player player, long amount) {
        PlayerAllianceGroup allianceGroup = player.getPlayerAllianceGroup2();
        if (allianceGroup != null) {
            allianceGroup.onEvent(new TeamKinahDistributionEvent<>(allianceGroup, player, amount));
        }
    }

    /**
     * Show specific mark on top of player
     */
    public static void showBrand(Player player, int targetObjId, int brandId) {
        PlayerAlliance alliance = player.getPlayerAlliance2();
        if (alliance != null) {
            alliance.onEvent(new ShowBrandEvent<>(alliance, targetObjId, brandId));
        }
    }

    public static String getServiceStatus() {
        return "Number of alliances: " + alliances.size();
    }

    public static class OfflinePlayerAllianceChecker implements Runnable, Predicate<PlayerAllianceMember> {

        private PlayerAlliance currentAlliance;

        @Override
        public void run() {
            for (PlayerAlliance alliance : alliances.values()) {
                currentAlliance = alliance;
                alliance.apply(this);
            }
            currentAlliance = null;
        }

        @Override
        public boolean apply(PlayerAllianceMember member) {
            if (!member.isOnline() && TimeUtil.isExpired(member.getLastOnlineTime() + GroupConfig.ALLIANCE_REMOVE_TIME * 1000)) {
                currentAlliance.onEvent(new PlayerAllianceLeavedEvent(currentAlliance, member.getObject(), LeaveReson.LEAVE_TIMEOUT));
            }
            return true;
        }
    }

}
