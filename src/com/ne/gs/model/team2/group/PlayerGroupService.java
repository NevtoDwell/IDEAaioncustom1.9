/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.func.tuple.Tuple1;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.events.PlayerLeavedEvent.LeaveReson;
import com.ne.gs.model.team2.common.events.ShowBrandEvent;
import com.ne.gs.model.team2.common.events.TeamKinahDistributionEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.model.team2.group.events.*;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.TimeUtil;

import static com.ne.gs.model.team2.group.callback.GroupCallbacks.*;

/**
 * @author ATracer
 */
public final class PlayerGroupService {

    private static final Logger log = LoggerFactory.getLogger(PlayerGroupService.class);

    private static final Map<Integer, PlayerGroup> groups = new ConcurrentHashMap<>();
    private static final AtomicBoolean offlineCheckStarted = new AtomicBoolean();

    public static void inviteToGroup(Player inviter, Player invited) {
        if (canInvite(inviter, invited)) {
            PlayerGroupInvite invite = new PlayerGroupInvite(inviter, invited);
            if (invited.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_PARTY_DO_YOU_ACCEPT_INVITATION, invite)) {
                invited.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_PARTY_DO_YOU_ACCEPT_INVITATION, 0, 0, inviter.getName()));
            }
        }
    }

    public static boolean canInvite(Player inviter, Player invited) {
        return RestrictionsManager.canInviteToGroup(inviter, invited);
    }

    public static PlayerGroup createGroup(Player leader, Player invited) {
        EventNotifier.GLOBAL.fire(BeforeCreate.class, leader);

        PlayerGroup newGroup = new PlayerGroup(new PlayerGroupMember(leader));
        groups.put(newGroup.getTeamId(), newGroup);
        addPlayer(newGroup, leader);
        addPlayer(newGroup, invited);
        if (offlineCheckStarted.compareAndSet(false, true)) {
            initializeOfflineCheck();
        }

        EventNotifier.GLOBAL.fire(AfterCreate.class, leader);
        return newGroup;
    }

    private static void initializeOfflineCheck() {
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new OfflinePlayerChecker(), 1000, 30 * 1000);
    }

    public static void addPlayerToGroup(PlayerGroup group, Player invited) {
        EventNotifier.GLOBAL.fire(BeforeEnter.class, Tuple2.of(group, invited));

        group.addMember(new PlayerGroupMember(invited));

        EventNotifier.GLOBAL.fire(AfterEnter.class, Tuple2.of(group, invited));
    }

    /**
     * Change group's loot rules and notify team members
     */
    public static void changeGroupRules(PlayerGroup group, LootGroupRules lootRules) {
        group.onEvent(new ChangeGroupLootRulesEvent(group, lootRules));
    }

    /**
     * Player entered world - search for non expired group
     */
    public static void onPlayerLogin(Player player) {
        for (PlayerGroup group : groups.values()) {
            PlayerGroupMember member = group.getMember(player.getObjectId());
            if (member != null) {
                group.onEvent(new PlayerConnectedEvent(group, player));
            }
        }
    }

    /**
     * Player leaved world - set last online on member
     */
    public static void onPlayerLogout(Player player) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            PlayerGroupMember member = group.getMember(player.getObjectId());
            member.updateLastOnlineTime();
            group.onEvent(new PlayerDisconnectedEvent(group, player));
        }
    }

    /**
     * Update group members to some event of player
     */
    public static void updateGroup(Player player, GroupEvent groupEvent) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            group.onEvent(new PlayerGroupUpdateEvent(group, player, groupEvent));
        }
    }

    /**
     * Add player to group
     */
    public static void addPlayer(PlayerGroup group, Player player) {
        Preconditions.checkNotNull(group, "Group should not be null");
        group.onEvent(new PlayerEnteredEvent(group, player));
    }

    /**
     * Remove player from group (normal leave, or kick offline player)
     */
    public static void removePlayer(Player player) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            group.onEvent(new PlayerGroupLeavedEvent(group, player));
        }
    }

    /**
     * Remove player from group (ban)
     */
    public static void banPlayer(Player bannedPlayer, Player banGiver) {
        Preconditions.checkNotNull(bannedPlayer, "Banned player should not be null");
        Preconditions.checkNotNull(banGiver, "Bangiver player should not be null");
        PlayerGroup group = banGiver.getPlayerGroup2();
        if (group != null) {
            if (group.hasMember(bannedPlayer.getObjectId())) {
                group.onEvent(new PlayerGroupLeavedEvent(group, bannedPlayer, LeaveReson.BAN, banGiver.getName()));
            } else {
                log.warn("TEAM2: banning player not in group {}", group.onlineMembers());
            }
        }
    }

    /**
     * Disband group by removing all players one by one
     */
    public static void disband(PlayerGroup group) {
        EventNotifier.GLOBAL.fire(BeforeDisband.class, group);

        Preconditions.checkState(group.onlineMembers() <= 1, "Can't disband group with more than one online member");
        groups.remove(group.getTeamId());
        group.onEvent(new GroupDisbandEvent(group));

        EventNotifier.GLOBAL.fire(AfterDisband.class, group);
    }

    /**
     * Share specific amount of kinah between group members
     */
    public static void distributeKinah(Player player, long kinah) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            group.onEvent(new TeamKinahDistributionEvent<>(group, player, kinah));
        }
    }

    /**
     * Show specific mark on top of player
     */
    public static void showBrand(Player player, int targetObjId, int brandId) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            group.onEvent(new ShowBrandEvent<>(group, targetObjId, brandId));
        }
    }

    public static void changeLeader(Player player) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            group.onEvent(new ChangeGroupLeaderEvent(group, player));
        }
    }

    /**
     * Start mentoring in group
     */
    public static void startMentoring(Player player) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            group.onEvent(new PlayerStartMentoringEvent(group, player));
        }
    }

    /**
     * Stop mentoring in group
     */
    public static void stopMentoring(Player player) {
        PlayerGroup group = player.getPlayerGroup2();
        if (group != null) {
            group.onEvent(new PlayerGroupStopMentoringEvent(group, player));
        }
    }

    public static void cleanup() {
        log.info(getServiceStatus());
        groups.clear();
    }

    public static String getServiceStatus() {
        return "Number of groups: " + groups.size();
    }

    public static PlayerGroup searchGroup(Integer playerObjId) {
        for (PlayerGroup group : groups.values()) {
            if (group.hasMember(playerObjId)) {
                return group;
            }
        }
        return null;
    }

    public static class OfflinePlayerChecker implements Runnable, Predicate<PlayerGroupMember> {

        private PlayerGroup currentGroup;

        @Override
        public void run() {
            for (PlayerGroup group : groups.values()) {
                currentGroup = group;
                group.apply(this);
            }
            currentGroup = null;
        }

        @Override
        public boolean apply(PlayerGroupMember member) {
            if (!member.isOnline() && TimeUtil.isExpired(member.getLastOnlineTime() + GroupConfig.GROUP_REMOVE_TIME * 1000)) {
                // TODO LEAVE_TIMEOUT type
                currentGroup.onEvent(new PlayerGroupLeavedEvent(currentGroup, member.getObject()));
            }
            return true;
        }
    }

}
